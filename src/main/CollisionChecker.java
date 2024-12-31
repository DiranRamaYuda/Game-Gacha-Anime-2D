package main;

import entity.Entity;
import tile.TilePanel;

public class CollisionChecker {
    TilePanel tileP;

    public CollisionChecker(TilePanel tileP) {
        this.tileP = tileP;
    }

    // CEK TILE SOLID
    public void checkTile(Entity entity) {
        int entityLeftX = entity.x + entity.solidArea.x;
        int entityRightX = entity.x + entity.solidArea.x + entity.solidArea.width;
        int entityTopY = entity.y + entity.solidArea.y;
        int entityBottomY = entity.y + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftX / tileP.tileSize;
        int entityRightCol = entityRightX / tileP.tileSize;
        int entityTopRow = entityTopY / tileP.tileSize;
        int entityBottomRow = entityBottomY / tileP.tileSize;

        int tileNum1, tileNum2;

        switch (entity.direction) {
            case "up":
                entityTopRow = (entityTopY - entity.speed) / tileP.tileSize;
                tileNum1 = tileP.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = tileP.mapTileNum[entityRightCol][entityTopRow];
                if (tileP.tile[tileNum1].collision == true || tileP.tile[tileNum2].collision == true) {
                    entity.collisionOn = true;
                }
                break;
            case "down":
                entityBottomRow = (entityBottomY + entity.speed) / tileP.tileSize;
                tileNum1 = tileP.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = tileP.mapTileNum[entityRightCol][entityBottomRow];
                if (tileP.tile[tileNum1].collision == true || tileP.tile[tileNum2].collision == true) {
                    entity.collisionOn = true;
                }
                break;
            case "left":
                entityLeftCol = (entityLeftX - entity.speed) / tileP.tileSize;
                tileNum1 = tileP.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = tileP.mapTileNum[entityLeftCol][entityBottomRow];
                if (tileP.tile[tileNum1].collision == true || tileP.tile[tileNum2].collision == true) {
                    entity.collisionOn = true;
                }
                break;
            case "right":
                entityRightCol = (entityRightX + entity.speed) / tileP.tileSize;
                tileNum1 = tileP.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = tileP.mapTileNum[entityRightCol][entityBottomRow];
                if (tileP.tile[tileNum1].collision == true || tileP.tile[tileNum2].collision == true) {
                    entity.collisionOn = true;
                }
                break;
        }
    }
}
