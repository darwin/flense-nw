# flense-nw

flense-nw is a Clojure code editor app written using [node-webkit](https://github.com/rogerwang/node-webkit) and [Flense](https://github.com/mkremins/flense). Essentially, flense-nw wraps an instance of the baseline Flense editor component in an imitation of the traditional text editor interface, providing functionality like file I/O, configurable keybinds, and a way to enter text commands.

## Building

flense-nw runs on [node-webkit](https://github.com/rogerwang/node-webkit). You'll also need [npm](https://www.npmjs.org/) to install some of the dependencies and [Leiningen](http://leiningen.org/) to compile the ClojureScript source.

For the time being, flense-nw builds against the latest snapshot version of Flense. It's recommended that you check out the [Flense repo](https://github.com/mkremins/flense) and `lein install` it in your local repository before attempting to build flense-nw.

```bash
cd path/to/flense
lein cljsbuild once
npm install
path/to/node-webkit .
```

This will build flense-nw from source and launch it as a standalone GUI app.

### Development tips

#### Development cycle with flense subproject

If you want to develop on both flense and flense-nw concruently without constantly launching `lein install` in flense. I recommend including flense as a subproject in flense-nw. Leiningen has a feature called [checkout dependencies](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md#checkout-dependencies) for that. 

##### Initial setup:

    cd path/to/flense-nw
    mkdir checkouts
    cd checkouts
    ln -s path/to/flense flense
    

My tree looks like this (flense repo is checked out in `$CODE/flense-dev/flense`):

    $CODE/flense-dev/flense-nw ➔ tree .
    .
    ├── LICENSE
    ├── README.md
    ├── checkouts
    │   └── flense -> ../../flense
    ├── index.html
    ├── package.json
    ├── project.clj
    ├── resources
    ...

Unfortunately this is not yet fully supported for cljs tooling.

Sam Aaron suggests using [a hybrid solution](https://groups.google.com/forum/#!msg/clojurescript/KRL-rJudhbg/7Fd39KgN2T8J) to modify `:source-paths` to get it working.

With `:source-paths` modification, running `lein cljsbuild auto` will pick up all file changes in both flense-nw and flense. Also flense folder will be on classpath and will take precence before locally installed flence library thanks to checkout dependencies system.

(happy face)

#### Using React Development Tools

Flense uses Facebook's [React.js](https://github.com/facebook/react) library (via David Nolen's [Om](https://github.com/swannodette/om)). React team offers useful [React Developer Tools](https://github.com/facebook/react-devtools) (RDT), which is a Chrome extension for inspecting and debugging React components (it integrates into Chrome's dev tools). Flense-nw is running in node-webkit (aka [nw.js]((https://github.com/nwjs/nw.js))). The problem is that RDT cannot be easily installed into nw.js itself.

A solution is to use standalone devtools (frontend) in a standalone Chrome with RDT extension installed and instruct devtools to connect to our remote backend (our nw.js context running inside Flense-nw). It is easily doable, but it requires a special setup:

##### Preparation

  * launch `/Applications/Google\ Chrome\ Canary.app/Contents/MacOS/Google\ Chrome\ Canary --no-first-run --user-data-dir=~/temp/chrome-dev-profile`
  * install RDT
  
##### Development workflow

1. run node-webkit instance with remote debugging enabled:

        cd path/to/flense-nw
        path/to/node-webkit --remote-debugging-port=9222 .
        
2. launch `/Applications/Google\ Chrome\ Canary.app/Contents/MacOS/Google\ Chrome\ Canary --no-first-run --user-data-dir=~/temp/chrome-dev-profile`
3. in Chrome navigate to [http://localhost:9222/json](http://localhost:9222/json)
    => you should see a websocket url for remote context running in your nw.js from step #1 (note: sometimes you have to do a second refresh to see devtoolsFrontendUrl):

        [ {
           "description": "",
           "devtoolsFrontendUrl": "/devtools/devtools.html?ws=localhost:9222/devtools/page/BDFB0179-D7E4-6A27-6AD4-D7039548FDCB",
           "id": "BDFB0179-D7E4-6A27-6AD4-D7039548FDCB",
           "title": "index.html",
           "type": "page",
           "url": "file:///Users/darwin/code/flense-dev/flense-nw/index.html",
           "webSocketDebuggerUrl": "ws://localhost:9222/devtools/page/BDFB0179-D7E4-6A27-6AD4-D7039548FDCB"
        } ]
        
4. in Chrome navigate to devtoolsFrontendUrl where you replace `/devtools/devtools.html` with `chrome-devtools://devtools/bundled/devtools.html` (kudos to Paul Irish for the solution)
    
    example: `chrome-devtools://devtools/bundled/devtools.html?ws=localhost:9222/devtools/page/BDFB0179-D7E4-6A27-6AD4-D7039548FDCB`

Voila! Now you should have a debug session estabilished between your devtools in Chrome (devtools frontend) and your Flense-nw application (devtools backend).

Last tested with Chrome Canary 42.0.2283.5 and RDT 0.12.1.

## License

[MIT License](http://opensource.org/licenses/MIT). Hack away.
