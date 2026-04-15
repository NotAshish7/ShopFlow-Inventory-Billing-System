@echo off
cd /d "%~dp0"
cd "Program Files"
echo Compiling ShopFlow...
javac -d bin -encoding UTF-8 "Inventory & Billing System.java"
echo Starting ShopFlow...
start javaw -cp bin InventoryManagementSystem
