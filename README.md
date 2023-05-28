# NES Emulator

## How to run
 * Install Java 17 or later and Maven if not already installed
 * From the base directory execute the command: mvn clean package javafx:run
 * From the "File" menu select "Load ROM" and select the appropriate ROM file (no ROMs are included in this repository)
 
## Default controls
| NES Key  | Controller 0  | Controller 1  |
| -------- | ------------- | ------------- |
| A        | Z             | Q             |
| B        | X             | W             |
| Up       | Up            | Numpad 5      |
| Down     | Down          | Numpad 2      |
| Left     | Left          | Numpad 1      | 
| Right    | Right         | Numpad 3      |
| Select   | C             | E             |
| Start    | V             | R             |
 
## Working mappers with list of games tested:
 * Mapper 0 (NROM) : Super Mario Bros, Donkey Kong, Dig Dug
 * Mapper 1 (MMC1) : The Legend of Zelda, Castlevania 2, Tetris
 * Mapper 2 (UNROM) : Castlevania, Contra, Mega Man
 * Mapper 3 (CNROM) : Arkanoid, Solomon's Key
 * Mapper 4 (MMC3) : Super Mario Bros 3
 * Mapper 7 (AxROM) : Battletoads (Contains visual glitches), Wizards & Warriors

## To do:
* Add sounds
* Additional mappers later

