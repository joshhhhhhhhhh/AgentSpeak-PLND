This is an extension of Peleus (AgentSpeak-PL) which can automatically generate plans using a FOND and POND Planner

# SETUP

This extension is built using Docker, so make sure it is installed prior to attempting to run this extension. 

After it has been installed, in the root directory of this repo run the following command:

```
docker build . -t peleus:latest; docker run peleus:latest
```

This will create the docker image and subsequently run jason with the extension. The Jason project being run cane be specified in the **run** task found near the bottom of the build.gradle file. 

# CREATING AN AGENTSPEAK FILE

In order for this extension to be used, a few changes to the AgentSpeak file are required:

## Planning Object Definitions

These are the objects which will be used in the planning problem. They are beliefs denoted as 

```
object(type, value)
```
Which can be initialized in either the AgentSpeak file or from the Environment (or a combination of them).

## Action Definitions

Any actions used by the agent as part of the planning problem need to be described in their AgentSpeak file as plans. Examples of them can be found in MAPC.asl, gridAgent.asl and vacuum.asl.

Note that the planners do not support numerics, so actions need to be described without assuming variables are numbers (eg no X + 1).

## Multiple World Definitions

In a partially observable case, the possible values for beliefs are defined using AgentSpeak-DEL's **range**, examples of which are found in mapc.asl and gridAgent.asl. 

This means that when using them, use numerics in the beliefs/objects etc, and then describe the actions using non-numeric variables, examples of which are again found in the previously mentioned files.

