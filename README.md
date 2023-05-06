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
- For modders, it'll be `/data/cristellib`.

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
    "ancient_cities": "A dark city at the bottom of the World",
    "buried_treasures.spacing": "Set the spacing of the buried treasure structure set"
  },
  "structure_sets": [
    {
      "modid": "minecraft",
      "structure_set": [
        "minecraft:ancient_cities",
        "minecraft:buried_treasures"
      ]
    },
    {
      "modid": "t_and_t",
      "structure_set": [
        "towns_and_towers:towers",
        "towns_and_towers:towns"
      ]
    }
  ]
}
```
#### Required Fields
- **"subPath"** specifies the sub folder in the config folder where the file will be located. E.g. if you put there "cristellib" your config will be in `/config/cristellib/`.
- **"config_type"** specifies, if the config will be for editing the placement (`PLACEMENT`), or if it is for enabling/disabling (`ENABLE_DISABLE`) a structure (there is no combined version yet).
- **"structure_sets"** in this array you have to put every structure set, which should be in the config. The **"modid"** specifies in which mod container the structure set file should be searched, and the **"structure_set"** parameter defines the location.

#### Optional Fields
- **"name"** changes the name of the config file.
- **"header"** is the text, which will be at the top of your config file.
- **"comments"** is a map where the keys indicate the location where the comment should appear.
