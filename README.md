图书管理系统 - 座位管理功能修复与增强说明
修复的问题
1. index.html 语法错误
问题： 文件末尾有多余的 和 代码});</script></html>
修复： 删除多余代码
2. 阅览室硬编码问题
问题: 座位列表页面的阅览室选择框是硬编码的，只有"1号阅览室"和"2号阅览室"
修复： 改为从数据库动态获取阅览室列表
3. reading_room表未被使用
问题： 数据库中有reading_room表，但代码中没有使用
修复： 创建了ReadingRoom实体类，并在SeatMapper中添加了相关作
新增功能
管理员座位管理功能
管理员可以在"座位管理"菜单中：

座位管理 (/seat/manage)

查看所有座位
添加单个座位
批量添加座位（支持设置前缀、起始编号、数量）
编辑座位信息
删除座位（有用户使用时无法删除）
强制释放座位
按阅览室/状态筛选
阅览室管理 (/seat/room-manage)

查看所有阅览室
添加阅览室
编辑阅览室信息
删除阅览室（有座位时无法删除）
启用/禁用阅览室
权限控制
普通用户只能看到"座位预约"和"我的预约"
管理员可以看到完整的"座位管理"和"阅览室管理"菜单
修改的文件清单
新增文件
src/main/java/com/book/manager/entity/ReadingRoom.java- 阅览室实体类
src/main/resources/templates/seat/seat-manage.html- 座位管理页面（管理员）
src/main/resources/templates/seat/room-manage.html- 阅览室管理页面（管理员）
db_update.sql- 数据库更新脚本
修改文件
src/main/java/com/book/manager/dao/SeatMapper.java- 扩展了座位和阅览室的数据库操作
src/main/java/com/book/manager/controller/SeatController.java- 扩展了座位和阅览室的API接口
src/main/resources/templates/seat/seat-list.html- 座位列表改为动态获取阅览室
src/main/resources/templates/index.html- 修复语法错误，添加管理员菜单
API接口
用户接口
GET /seat/list- 座位列表页面
GET /seat/my- 我的预约页面
POST /seat/reserve- 预约座位
POST /seat/leave- 签退/释放座位
GET /seat/rooms- 获取阅览室列表
管理员接口
GET /seat/manage- 座位管理页面
GET /seat/room-manage- 阅览室管理页面
POST /seat/admin/addSeat- 添加座位
POST /seat/admin/batchAddSeat- 批量添加座位
POST /seat/admin/updateSeat- 更新座位
POST /seat/admin/deleteSeat- 删除座位
POST /seat/admin/forceRelease- 强制释放座位
GET /seat/admin/seats- 获取所有座位
POST /seat/admin/addRoom- 添加阅览室
POST /seat/admin/updateRoom- 更新阅览室
POST /seat/admin/deleteRoom- 删除阅览室
POST /seat/admin/toggleRoom- 切换阅览室启用状态
GET /seat/admin/rooms- 获取所有阅览室
使用说明
数据库准备

确保已执行 或 脚本book_manager.sqldb_update.sql
reading_room、seat、seat_reservation 表应该已存在
启动项目

使用IDE导入项目或执行mvn spring-boot:run
访问 http://localhost:8080
管理员操作

使用管理员账号登录（is_admin=0）
在左侧菜单找到"座位管理"
先到"阅览室管理"添加阅览室
再到"座位管理"添加座位
普通用户操作

使用普通账号登录（is_admin=1）
在"座位预约"页面选择空闲座位进行预约
在"我的预约"页面可以查看预约记录和签退
注意事项
一个用户同时只能预约一个座位
删除阅览室前需要先删除该阅览室下的所有座位
删除座位前确保没有用户正在使用
管理员可以强制释放正在使用的座位
