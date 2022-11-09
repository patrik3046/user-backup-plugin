# How to use the plugin

## Set the directory for the files

Open the `../User-Backup-Plugin/src/main/resources/jsons.path` file and replace the whole context with your own
directory's absolute path.

## Set the realm name

Open the `../User-Backup-Plugin/src/main/resources/realm.name` file and replace the whole context with the realm's name
which will have the plugin.

## Build the project

To build the project open the terminal in the `User-Backup-Plugin` folder and run the following command: 
```
./gradlew jar
```
. After you built the project you can find the jar file in the `build/libs` folder.

## Deploy the jar file

Put your jar file into the `$[keycloak_home]/standalone/deployments` folder. The server will automatically deploy it.

## Select the plugin

On your Keycloak admin admin panel select the realm which you want to use the plugin. Select the `Events` option from
the side menu and open the `Config` tab. Here you can add a new Event Listener by clicking inside the box. Select your
plugin and save.

# The plugin's features

## Export files

The plugin exports a file when a user logging in or update his/her own profile. If the admin add or update an user then
the plugin exports a file too.

## Restore

If your realm is lost,deleted or something happened then you need to restore the files.  
There is two option:

* Recommended: [optional:reinstall the server and add the plugin to it] -> create a realm with the same name like the
  other one had -> Import the export files -> Add the plugin into the realm -> call the restore
  endpoint: `$HOST/auth/realms/$REALM_NAME/restore`
* [optional:reinstall the server and add the plugin to it] -> create a realm with the same name like the other one had
  -> Add the plugin into the realm ->  Import the export files -> call the restore
  endpoint: `$HOST/auth/realms/$REALM_NAME/restore`

## Metrics

The plugin has their own metrics what you can see if you call the following
endpoint: `$HOST/auth/realms/$REALM_NAME/backup-metrics`
