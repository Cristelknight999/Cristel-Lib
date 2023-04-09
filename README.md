# Cristel Lib
A Library Mod for Structure configs

## Features
- Easy Structure Config System
- Data support
- Runtime Datapack
- Datapack Loading

## Using Cristel Lib with data
First create the folder where you will put your files for cristellib.
- For modpack makers, it'll be in the instance folder, in `config/cristellib/data`.
- For modders, it'll be `/data/YOURMODID`.

### Creating a structure config
In your folder you'll have to make another called `structure_configs`. In this folder you will create files which will add configs for the structures you want.
To create a config create a new json file the name of the file will be the name of the config file later.
The file structure should look like this:
```
{
  "name": "",
  "subPath": "",
  "header": "",
  "config_type": "",
  "comments": {
  },
  "structure_sets": [
    {
      "modid": "minecraft",
      "structure_set": "minecraft:ancient_cities"
    },
    {
      "modid": "minecraft",
      "structure_set": "minecraft:buried_treasures"
    }
  ]
}
```
#### Required Fields
- "subPath" specifies the sub folder in the config folder which the file will be located. E.g. if you put there "cristellib" your config will be in `/config/cristellib/`.
- "config_type" specifies, if the config will be for editing the placement (PLACEMENT), or if it is for enabling/disabling a structure (there is no combined version yet).
#### Optional Fields
