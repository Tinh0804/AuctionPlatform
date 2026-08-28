import fs from 'fs';
import path from 'path';

function walkDir(dir, callback) {
  fs.readdirSync(dir).forEach(f => {
    let dirPath = path.join(dir, f);
    let isDirectory = fs.statSync(dirPath).isDirectory();
    isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
  });
}

walkDir('./src', function(filePath) {
  if (filePath.endsWith('.js') || filePath.endsWith('.jsx')) {
    let content = fs.readFileSync(filePath, 'utf8');
    let updated = content
      .replace(/@\/routes/g, '@/app/routes')
      .replace(/@\/layouts/g, '@/app/layouts')
      .replace(/..\/..\/components\/Auction\/AuctionCard/g, '@/entities/auction/AuctionCard')
      .replace(/..\/..\/components\/Elements\/Skeleton/g, '@/shared/ui/Skeleton');

    if (content !== updated) {
      fs.writeFileSync(filePath, updated, 'utf8');
      console.log('Updated ' + filePath);
    }
  }
});
