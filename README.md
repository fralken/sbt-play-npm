# sbt-play-npm

Integrate a Npm application with Play (version 2.6).

A typical use case is to have a single page application (e.g. implemented with React) that calls a set of Rest api implemented in Play.
During development you set up a Npm project for tha SPA, and a Play project for the Rest api, being careful to proxy the Play server behind the Node server to avoid any CORS issue.

At runtime the SPA is served by an http server (e.g. Nginx) which again should proxy the Play server for the same reason.

This is quite easy to set up, but has the following disadvantages:

- the two projects' lifecycles must be kept in sync
- an http server should be put in front of the Play server, unless you configure CORS filter so that the front end application can call api served by a different server

This is not a big deal, but it would be nice to:
 
- have a single sbt project that contains both the SPA and the api, still using Npm for the development of the SPA
- spawn both a Node server and a Play server (almost) seamlessly when running `sbt run`
- invoke tests for both npm and sbt when running `sbt test`
- package both javascript and scala code in the same package when running `sbt dist` or `sbt stage`
- have the Play server to serve also the SPA at runtime, without the need of another http server

This plugin helps implement this easily.

## Usage

You can see an example of a very simple application using this plugin in the `example` folder.

Add the plugin to your project by adding these lines in `project/project.sbt`

```
resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("eu.unicredit" % "sbt-play-npm" % "0.1")
```

Place your npm project in a folder named `web` (by default, this can be changed)

#### Node server should proxy Rest api calls in Dev Mode

In the `package.json` of your npm project you should add the following line

```
"proxy": "http://localhost:9000"
```

This let the Node server run in dev mode to proxy requests to the Rest api served by Play (at port 9000).
It is not used at runtime since there's not a Node server.

Now you can run both frontend and backend applications with `sbt run`. A Node server at port 3000 is spawned for the SPA, and a Play server at port 9000 is running for the api. Both are in watch mode.

You can test both frontend and backend applications with `sbt test`.

#### Play should serve Javascript assets in Prod Mode

You have to add routes to serve the `index.html` page usually at root (`/`), and routes for static assets (css, png, js, etc). All other paths should represent front end routes and should again return `index.html` (which manages routes on the browser).
This can be done by defining a `Router` in Scala like so:

```
package routers

import javax.inject.Inject

import controllers.Assets
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class WebRouter @Inject()(assets: Assets) extends SimpleRouter
{
  override def routes: Routes = {
    case GET(p"/") => assets.at("index.html")
    case GET(p"/$file<[^.]+([.][^.]+)+>") => assets.at(file)
    case GET(p"/$route*") => assets.at("index.html")
  }
}
```

This defines three routes as explained above, where static assets are recognized with a regular expression that matches strings containing one or more ".". This can be changed e.g. by specifying a static path if you are sure static assets are served at that path.

Then you have to activate these routes by adding in your `routes` file, as the last route:

```
->    /       routers.WebRouter
```

`sbt stage` builds both the npm application and scala application, `sbt dist` packages everything.

Finally, `sbt clean` cleans also the build folder of the npm application. 

## Settings

The following settings are available:

- `npmDependenciesInstall`: command to install dependencies (default `npm install`)
- `npmTest`: command to run tests (default `npm run test`)
- `npmServe`: command to start the application (defualt `npm run start`)
- `npmBuild`: command to build the application (default `npm run build`)
- `npmEnvOption`: optional environment option to add to the command (default `Some("CI=true")`). This is useful so that npm tests are not interactive (otherwise they would block waiting for user input)
- `npmSrcDir`: folder name of the application source files (default `web`)
- `npmModulesDir`: folder name of the dependency modules (default `/node_modules`)
- `npmBuildDir`: folder name of the generated build files (default `/build`)

## Tasks

The following tasks are available:

- `npmTestTask`: task to run tests. This is automatically executed before the `test` task
- `npmBuildTask`: task to build the application. This is automatically executed before the `dist` and `stage` tasks
- `npmCleanTask`: task to clean the build folder. This is automatically executed before the `clean` task

## Known issues

When running `sbt run`, pressing ctrl-D to stop the server may not stop the Node server completely. In this case you have to kill the Node process manually.
As a workaround you can press ctrl-C instead, which exits sbt completely. Alternatively, yuo can change the start command in `package.json`.

For example, if you created a React application with `create-react-app` (like in `example` project), in `package.json` you'll probably have script definitions like so:

```
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test --env=jsdom",
    "eject": "react-scripts eject"
  }
```

`react-scripts start` actually starts another process `node node_modules/react-scripts/scripts/start.js`, so you can replace the command with this one:

```
  "scripts": {
    "start": "node node_modules/react-scripts/scripts/start.js",
    "build": "react-scripts build",
    "test": "react-scripts test --env=jsdom",
    "eject": "react-scripts eject"
  }
```

Now the Node process is created directly (and not as a subprocess), so pressing ctrl-D it is possible to terminate it.
