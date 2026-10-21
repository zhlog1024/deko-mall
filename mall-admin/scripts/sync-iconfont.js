import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const srcDir = path.join(__dirname, '../src/assets/icons');
const destDir = path.join(__dirname, '../public/static/iconfont');

const files = ['iconfont.css', 'iconfont.js', 'iconfont.json', 'iconfont.ttf'];

// 确保目标目录存在
if (!fs.existsSync(destDir)) {
  fs.mkdirSync(destDir, { recursive: true });
  console.log('📁 Created directory:', destDir);
}

let syncedCount = 0;
let skippedCount = 0;

files.forEach(file => {
  const srcFile = path.join(srcDir, file);
  const destFile = path.join(destDir, file);
  
  if (fs.existsSync(srcFile)) {
    fs.copyFileSync(srcFile, destFile);
    syncedCount++;
    console.log(`✅ Synced: ${file}`);
  } else {
    skippedCount++;
    console.warn(`⚠️  Not found: ${file}`);
  }
});

console.log(`\n✨ Iconfont sync completed! (${syncedCount} synced, ${skippedCount} skipped)`);
