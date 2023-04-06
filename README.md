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
`{
  "name": "",
  "subPath": "cristellib",
  "header": "some header",
  "config_type": "PLACEMENT",
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
}`
