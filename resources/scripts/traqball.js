/*
 *  traqball 2.2
 *  written by Dirk Weber
 *  http://www.eleqtriq.com/
 *  See demo at: http://www.eleqtriq.com/wp-content/static/demos/2011/traqball2011

 *  Copyright (c) 2011 Dirk Weber (http://www.eleqtriq.com)
 *  Licensed under the MIT (http://www.eleqtriq.com/wp-content/uploads/2010/11/mit-license.txt)
 */

/* terribly HACKED by darwin */

(function(){
    var userAgent   = navigator.userAgent.toLowerCase(),
        canTouch    = "ontouchstart" in window,
        prefix      = cssPref = "";

    if(/webkit/gi.test(userAgent)){
        prefix = "-webkit-";
        cssPref = "Webkit";
    }else if(/msie | trident/gi.test(userAgent)){
        prefix = "-ms-";
        cssPref = "ms";
    }else if(/mozilla/gi.test(userAgent)){
        prefix = "-moz-";
        cssPref = "Moz";
    }else if(/opera/gi.test(userAgent)){
        prefix = "-o-";
        cssPref = "O";
    }else{
        prefix = "";
    }

    function bindEvent(target, type, callback, remove){
        //translate events
        var evType      = type || "touchend",
            mouseEvs    = ["mousedown", "mouseup", "mousemove"],
            touchEvs    = ["touchstart", "touchend", "touchmove"],
            remove      = remove || "add";

        evType = canTouch ? evType : mouseEvs[touchEvs.indexOf(type)];

        target[remove+"EventListener"](evType, callback, false);
    }

    function getCoords(eventObj){
        var xTouch,
            yTouch;

        if(eventObj.type.indexOf("mouse") > -1){
            xTouch = eventObj.pageX;
            yTouch = eventObj.pageY;
        }else if(eventObj.type.indexOf("touch") > -1){
            //only do stuff if 1 single finger is used:
            if(eventObj.touches.length === 1){
                var touch   = eventObj.touches[0];
                xTouch      = touch.pageX;
                yTouch      = touch.pageY;
            }
        }

        return [xTouch, yTouch];
    }

    function getStyle(target, prop){
        var style = document.defaultView.getComputedStyle(target, "");
        return style.getPropertyValue(prop);
    }

    var Traqball = function(confObj){
        this.config = {};
        this.box = null;

        this.setup(confObj);
    };

    Traqball.prototype.disable = function(){
        if(this.box !== null){
            bindEvent(this.box, 'touchstart', this.evHandlers[0], "remove");
            bindEvent(document, 'touchmove', this.evHandlers[1], "remove");
            bindEvent(document, 'touchend', this.evHandlers[2], "remove");
        }
    }

    Traqball.prototype.activate = function(){
        if(this.box !== null){
            bindEvent(this.box, 'touchstart', this.evHandlers[0]);
            bindEvent(document, 'touchmove', this.evHandlers[1], "remove");
            bindEvent(document, 'touchend', this.evHandlers[2], "remove");
        }
    }

    Traqball.prototype.setup = function(conf){
        var THIS            = this,
            stage,                  // the DOM-container of our "rotatable" element
            startCoords = [],
            bakedAngles = [0, 0],
            lastAngles = [0, 0];

        (function init (){
            THIS.disable();

            for(var prop in conf){
                THIS.config[prop] = conf[prop];
                }

            stage   = document.getElementById(THIS.config.stage) || document.getElementsByTagname("body")[0];

            //We parse viewport. The first block-element we find will be our "victim" and made rotatable
            for(var i=0, l=stage.childNodes.length; i<l; i++){
                var child = stage.childNodes[i];

                if(child.nodeType === 1){
                    THIS.box = child;
                    break;
                }
            }

            var perspective = getStyle(stage, prefix+"perspective"),
                pOrigin     = getStyle(stage, prefix+"perspective-origin"),
                bTransform  = getStyle(THIS.box, prefix+"transform");

            if(THIS.config.perspective){
                stage.style[cssPref+"Perspective"] = THIS.config.perspective;
            }else if(perspective === "none"){
                stage.style[cssPref+"Perspective"] = "700px";
            }

            if(THIS.config.perspectiveOrigin){
                stage.style[cssPref+"PerspectiveOrigin"] = THIS.config.perspectiveOrigin;
            }

            if (THIS.config.angles) {
              bakedAngles = THIS.config.angles;
            }
            
            setTransform();
            bindEvent(THIS.box, 'touchstart', startrotation);

            THIS.evHandlers = [startrotation, rotate, finishrotation];
        })();
        
        function normalizeAngles() {
            var minx = 0;
            var maxx = 90;
            var minz = 0;
            var maxz = 85;
            var rx = bakedAngles[0]+lastAngles[0];
            var rz = bakedAngles[1]+lastAngles[1];
            if (rx<minx) {
              lastAngles[0] = minx -bakedAngles[0];
            }
            if (rx>maxx) {
              lastAngles[0] = maxx - bakedAngles[0];
            }
            if (rz<minz) {
              lastAngles[1] = minz - bakedAngles[1];
            }
            if (rz>maxz) {
              lastAngles[1] = maxz - bakedAngles[1];
            }
        }

        function setTransform() {
            var rx = bakedAngles[0]+lastAngles[0];
            var rz = bakedAngles[1]+lastAngles[1];
            THIS.box.style[cssPref+"Transform"] = "rotateX("+ rx +"deg) rotateY(0deg) rotateZ(" + rz + "deg)"
        }

        function startrotation(e){
            e.preventDefault();

            startCoords = getCoords(e);

            bindEvent(THIS.box,'touchstart', startrotation, "remove");
            bindEvent(document, 'touchmove', rotate);
            bindEvent(document, 'touchend', finishrotation);
        }

        function finishrotation(e){
            bindEvent(document, 'touchmove', rotate, "remove");
            bindEvent(document, 'touchend', finishrotation, "remove");
            bindEvent(THIS.box, 'touchstart', startrotation);
            
            bakedAngles[0] = bakedAngles[0]+lastAngles[0];
            bakedAngles[1] = bakedAngles[1]+lastAngles[1];
            lastAngles = [0, 0];
        }

        // The rotation:
        function rotate(e){
            var eCoords = getCoords(e);
            e.preventDefault();

            lastAngles = [eCoords[1] - startCoords[1], eCoords[0] - startCoords[0]];
            normalizeAngles();
            setTransform();
        }
    }

    window.Traqball = Traqball;
})();