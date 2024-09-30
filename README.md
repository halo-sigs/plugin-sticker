# Plugin-Sticker Halo 表情包插件

## 简介

这是为 Halo 博客系统开发的表情包管理插件。允许用户上传、管理和使用自定义表情包，增强博客的互动性和个性化。

## 功能特点

- 表情包分组管理
- 表情包上传和删除
- 表情包搜索功能
- 权限控制
- 与 Halo 富文本编辑器集成

## 安装

1. 下载最新的发布版本。
2. 在 Halo 管理界面中，导航至 `插件` -> `上传插件`。
3. 选择下载的插件文件并上传。
4. 启用插件。

## 使用方法

### 管理表情包

1. 在 Halo 管理界面中，找到 "表情包" 菜单项。
2. 您可以在这里创建新的表情包分组，或管理现有的分组。
3. 点击分组后，可以上传新的表情包或管理现有的表情包。

### 在编辑器中使用表情包

1. 在使用富文本编辑器时，找到表情包按钮。
2. 点击后会打开表情包选择器。
3. 选择想要插入的表情包，它会自动插入到当前光标位置。

## 开发

### 项目结构
```
.
├── src
│   ├── main
│   │   ├── java/run/halo/sticker
│   │   │   ├── endpoint
│   │   │   │   ├── StickerEndpoint.java
│   │   │   │   └── StickerGroupEndpoint.java
│   │   │   ├── infra
│   │   │   │   └── StickerSetting.java
│   │   │   ├── model
│   │   │   │   ├── Sticker.java
│   │   │   │   └── StickerGroup.java
│   │   │   ├── pojo
│   │   │   │   ├── enums
│   │   │   │   │   └── StickerSorter.java
│   │   │   │   └── query
│   │   │   │       └── StickerQuery.java
│   │   │   ├── reconciler
│   │   │   │   └── StickerReconciler.java
│   │   │   ├── service
│   │   │   │   ├── impl
│   │   │   │   │   └── StickerServiceImpl.java
│   │   │   │   └── StickerService.java
│   │   │   └── StickerPlugin.java
│   │   └── resources
│   │       ├── extensions
│   │       │   ├── attachment-local-policy.yaml
│   │       │   ├── role-template-sticker.yaml
│   │       │   └── settings.yaml
│   │       ├── logo.png
│   │       └── plugin.yaml
├── ui
│   ├── src
│   │   ├── components
│   │   │   ├── GroupEditingModal.vue
│   │   │   ├── LazyImage.vue
│   │   │   ├── StickerGroupList.vue
│   │   │   ├── StickerGroupListUser.vue
│   │   │   └── StickerPicker.vue
│   │   ├── editor
│   │   │   └── index.ts
│   │   ├── views
│   │   │   ├── StickerManage.vue
│   │   │   └── StickerManageUser.vue
│   │   └── index.ts
│   ├── package.json
│   └── vite.config.ts
├── build.gradle
├── gradle.properties
├── settings.gradle
└── README.md
```

### 构建

```bash
./gradlew build --stacktrace
```

### 打包

```bash
./gradlew jar
```

## API 参考

- `GET /apis/sticker.api.halo.run/v1alpha1/stickers`: 获取表情包列表
- `POST /apis/sticker.api.halo.run/v1alpha1/stickers/-/upload`: 上传新表情包
- `DELETE /apis/sticker.api.halo.run/v1alpha1/stickers/{name}`: 删除指定表情包
- `GET /apis/sticker.api.halo.run/v1alpha1/stickerGroups`: 获取表情包分组列表
- `POST /apis/sticker.api.halo.run/v1alpha1/stickerGroups`: 创建新的表情包分组
- `PUT /apis/sticker.api.halo.run/v1alpha1/stickerGroups/{name}`: 更新指定表情包分组
- `DELETE /apis/sticker.api.halo.run/v1alpha1/stickerGroups/{name}`: 删除指定表情包分组

## 许可证

本项目采用 [GPL-3.0 license](https://github.com/halo-sigs/plugin-sticker/blob/main/LICENSE)。
