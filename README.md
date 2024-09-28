# plugin-sticker

## 开发记录

### 模型设计

#### Sticker



### 接口设计

#### GET `/sticker/{stickerid}`
获取具体表情包 （所有人）

#### POST `/sticker/{username}/upload`
用户表情包上传 （本人）
#### DELETE `/sticker/{stickerid}`
删除用户表情包 （本人 | 管理员）

#### GET `/stickers/{username}/`
获取用户表情包列表 （本人 | 管理员）

#### GET `/stickergroup`
获取分组表情包列表（管理员）
#### POST `/stickergroup`
创建分组（管理员）
#### DELETE `/stickergroup/{groupname}`
删除分组（管理员）



