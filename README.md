otu
===

A simple, OT-compatible tool for doing some stuff with nexsons and trees.

Install
-----

####Clone the repo:

First, clone a copy of the OTU repo on your local machine.

```
git clone https://github.com/FePhyFoFum/otu.git
```

####Install dependencies

OTU requires the jade and ot-base repositories to be installed into the local maven directory. Run the following commands after you clone to download the dependencies from git and install them into your local maven repository cache. Of course, this requires maven (version 3).

Running this script will clone the ot-base and jade repos into the parent directory of the otu repo. To place them elsewhere, install them manually. See directions at https://github.com/FePhyFoFum/jade and https://github.com/OpenTreeOfLife/ot-base.

```
cd otu
sh mvn_install_dependencies.sh
```

OTU uses the jade and ot-base classes for many things. The source code for these dependencies is reasonably well-documented. You can refer to the class files in the jade and ot-base directories for more information. If you use Eclipse (with the m2eclipse plugin), just import the ot-base and jade repos as maven projects to browse their packages and classes.

####Set up OTU

To set up OTU for the first time, you can use the included setup script. This will download a copy of neo4j, build the OTU plugin, configure and start the neo4j server, and the start the OTU webserver that will interact with the neo4j database. The setup will run into problems if there is another instance of OTU or neo4j (or anything else) running on either of the the default ports 8000 or 7474.

Using the --force option tells the installer to put all the downloaded files at the default location, which is the parent directory of the current working directory (thus running it with --force from the otu directory installs in the parent of the otu directory itself). To install somewhere else, omit ```--force``` and add ```-prefix <prefix>``` to the arguments list.

```
sh setup_otu.sh --start-otu --restart-neo4j --open-otu --force
```

####Open OTU

Once the setup has been completed, you can point your web browser to http://localhost:8000/ to access the OTU interface.

####Additional information

The setup_otu.sh script can be used to simplify a number of development tasks. For a list of options, enter ```./setup_otu.sh --help```.
