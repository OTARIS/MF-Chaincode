![MetaHL Fabric](https://github.com/OTARIS/MF-Chaincode/blob/master/logo.png "MetaHL Fabric Logo")

# Chain Code #

The MetaHL Fabric Chain Code implements a data/asset management for your blockchain. It originates from the [NutriSafe](https://nutrisafe.de/ "NutriSafe") research project and is part of its official toolkit. You can read, write, update, create and delete objects in the blockchain network with simple commands and even define new object types (so called meta objects). Combined with the [MetaHL Fabric REST API](https://github.com/OTARIS/MF-REST-API/ "MetaHL Fabric REST API"), different roles from different use cases of an organization can be mapped to users with customizable access rights via the REST API for all chaincode functions.

Possible use cases include, but are not limited to:
* read access for the company's management dashboard
* automatic creation of new virtual products by supported machines (after physically creating a product)
* manual updates of product attributes by certified laboratories
* tracing product origins on the underlying supply chains

## Features ##

We continuously work on and extend the Chain Code. Combined with the [MetaHL Fabric REST API](https://github.com/OTARIS/MF-REST-API/ "MetaHL Fabric REST API") the following incomplete feature list might give you at least an idea about the capabilities:
* creating, deleting, updating and reading objects from the chain
* definition of new object types and possible attributes (meta objects)
* user management with roles and user assignable whitelists for restricting chaincode calls
* authentication with bruteforce force protection
* filtered selection of object IDs

## Installation ##

Before you install the chain code make sure your network is set up. 

We recommend to start with the startNetwork.sh script for fabric-version2x of the [NutriSafe network tools](https://github.com/NutriSafe-DLT/nutrisafe/ "NutriSafe Network").

      cd fabric-version2x/
      
      ./startNetwork.sh

For an easy installation of the chain code, you only need to copy the MetaHL Fabric Chain Code sources into the chaincode folder and run the installCC.sh script.

      cd chaincode/
      
      git clone https://github.com/OTARIS/MF-Chaincode/ MF-Chain-Code
      
      cd ..
      
      ./installCC.sh -c MF-Chain-Code

For easy use to the chain code, do not forget to check out the [MetaHL Fabric REST API](https://github.com/OTARIS/MF-REST-API/ "MetaHL Fabric REST API") repo!

## License ##

   Copyright 2021 OTARIS Interactive Services GmbH

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


## Third party ##

- Hyperledger® Fabric (Apache 2.0)
- Google® GSON (Apache 2.0)
