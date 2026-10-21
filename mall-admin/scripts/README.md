# Scripts 目录说明

## sync-iconfont.js

自动同步 iconfont 字体文件脚本。

### 功能

将 `src/assets/icons/` 目录下的 iconfont 相关文件同步到 `public/static/iconfont/` 目录,确保生产环境能正确访问字体资源。

### 同步的文件

- iconfont.css
- iconfont.js
- iconfont.json
- iconfont.ttf

### 使用方式

#### 1. 手动同步

```bash
npm run sync-iconfont
```

#### 2. 自动同步

脚本已集成到以下命令中,会自动执行:

- `npm run dev` - 开发环境启动前自动同步
- `npm run build:prod` - 生产环境构建前自动同步

### 为什么需要这个脚本?

在 Vite 项目中,`public` 目录下的文件会被原样复制到构建输出目录,路径保持不变。而 `src/assets` 中的文件在构建时会被添加 hash 值并重命名。

对于 iconfont 这种需要在 HTML 中通过 `<link>` 标签全局引用的资源,必须放在 `public` 目录才能确保生产环境路径正确。

### 注意事项

- 如果需要更新 iconfont,只需替换 `src/assets/icons/` 下的文件
- 运行 `npm run dev` 或 `npm run build:prod` 时会自动同步
- 也可以手动运行 `npm run sync-iconfont` 立即同步
