package com.book.manager.modules.ai.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Configuration
public class AiMemoryConfig {

    @Bean(name = "chatMemoryRedisTemplate")
    public RedisTemplate<String, String> chatMemoryRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> t = new RedisTemplate<>();
        t.setConnectionFactory(factory);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new StringRedisSerializer());
        t.afterPropertiesSet();
        return t;
    }

    /**
     * 仅用于 AI 记忆（Redis）序列化/反序列化的 ObjectMapper
     * 这里不要开启 DefaultTyping（否则会触发一堆 message 子类序列化问题）
     */
    @Bean(name = "chatMemoryObjectMapper")
    public ObjectMapper chatMemoryObjectMapper() {
        return new ObjectMapper();
    }

    /**
     * 我们自己存 Redis 的简化消息结构，避免 Jackson 直接序列化 LangChain4j 的 ChatMessage
     */
    public static class StoredMessage {
        private String type; // SYSTEM / USER / AI / TOOL / OTHER
        private String text;

        public StoredMessage() {
        }

        public StoredMessage(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @Bean
    public ChatMemoryStore chatMemoryStore(
            @Qualifier("chatMemoryRedisTemplate") RedisTemplate<String, String> redis,
            @Qualifier("chatMemoryObjectMapper") ObjectMapper om
    ) {
        return new ChatMemoryStore() {

            private String key(Object memoryId) {
                return "lc4j:chat:" + memoryId;
            }

            @Override
            public List<ChatMessage> getMessages(Object memoryId) {
                String json = redis.opsForValue().get(key(memoryId));
                if (json == null || json.isBlank()) return new ArrayList<>();

                try {
                    List<StoredMessage> stored = om.readValue(json, new TypeReference<List<StoredMessage>>() {});
                    List<ChatMessage> out = new ArrayList<>();
                    for (StoredMessage sm : stored) {
                        ChatMessage msg = toChatMessage(sm);
                        if (msg != null) out.add(msg);
                    }
                    return out;
                } catch (Exception e) {
                    // 解析失败就当没历史，避免影响主流程
                    return new ArrayList<>();
                }
            }

            @Override
            public void updateMessages(Object memoryId, List<ChatMessage> messages) {
                try {
                    List<StoredMessage> stored = new LinkedList<>();
                    if (messages != null) {
                        for (ChatMessage m : messages) {
                            if (m == null) continue;
                            stored.add(fromChatMessage(m));
                        }
                    }
                    String json = om.writeValueAsString(stored);
                    redis.opsForValue().set(key(memoryId), json);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void deleteMessages(Object memoryId) {
                redis.delete(key(memoryId));
            }

            private StoredMessage fromChatMessage(ChatMessage m) {
                String simple = m.getClass().getSimpleName(); // SystemMessage/UserMessage/AiMessage/ToolExecutionResultMessage...
                String type;
                if ("SystemMessage".equals(simple)) type = "SYSTEM";
                else if ("UserMessage".equals(simple)) type = "USER";
                else if ("AiMessage".equals(simple)) type = "AI";
                else if (simple != null && simple.toUpperCase().contains("TOOL")) type = "TOOL";
                else type = "OTHER";

                String text = extractTextByReflection(m);
                return new StoredMessage(type, text == null ? "" : text);
            }

            /**
             * 通过反射提取文本，兼容不同版本（有的叫 text()/singleText()/getText()/content()）
             */
            private String extractTextByReflection(ChatMessage m) {
                // 常见方法名优先级：text -> singleText -> getText -> content
                String[] candidates = new String[]{"text", "singleText", "getText", "content"};
                for (String name : candidates) {
                    try {
                        Method method = m.getClass().getMethod(name);
                        Object val = method.invoke(m);
                        if (val == null) continue;
                        // 如果是 String 直接返回
                        if (val instanceof String s) return s;
                        // 其他类型就 toString 兜底
                        return String.valueOf(val);
                    } catch (Exception ignored) {
                    }
                }
                // 兜底
                try {
                    return String.valueOf(m);
                } catch (Exception e) {
                    return "";
                }
            }

            /**
             * 读取 Redis 的 StoredMessage -> 恢复为 LangChain4j ChatMessage
             * 这里用反射 new 对象，避免你当前版本构造器/工厂方法差异导致编译不过
             */
            private ChatMessage toChatMessage(StoredMessage sm) {
                if (sm == null) return null;
                String text = sm.getText() == null ? "" : sm.getText();

                try {
                    switch (sm.getType()) {
                        case "SYSTEM":
                            return newMessage("dev.langchain4j.data.message.SystemMessage", text);
                        case "USER":
                            return newMessage("dev.langchain4j.data.message.UserMessage", text);
                        case "AI":
                            return newMessage("dev.langchain4j.data.message.AiMessage", text);
                        default:
                            // OTHER/TOOL：为了不污染上下文，这里可以选择丢弃或转成 AiMessage
                            // 我这里转成 AiMessage（保留一些可读信息）
                            return newMessage("dev.langchain4j.data.message.AiMessage", text);
                    }
                } catch (Exception e) {
                    return null;
                }
            }

            private ChatMessage newMessage(String className, String text) throws Exception {
                Class<?> clazz = Class.forName(className);
                // 优先走 (String) 构造器
                try {
                    Constructor<?> c = clazz.getConstructor(String.class);
                    Object obj = c.newInstance(text);
                    return (ChatMessage) obj;
                } catch (NoSuchMethodException ignored) {
                }
                // 再尝试静态 from(String)
                try {
                    Method from = clazz.getMethod("from", String.class);
                    Object obj = from.invoke(null, text);
                    return (ChatMessage) obj;
                } catch (NoSuchMethodException ignored) {
                }
                // 最后兜底：静态 of(String)
                Method of = clazz.getMethod("of", String.class);
                Object obj = of.invoke(null, text);
                return (ChatMessage) obj;
            }
        };
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(store)
                .build();
    }
}

