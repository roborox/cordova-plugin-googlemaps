var argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec'),
    common = require('./Common'),
    BaseClass = require('./BaseClass');

var PLUGIN_NAME = "KmlOverlay";

/*****************************************************************************
 * KmlOverlay Class
 *****************************************************************************/
var KmlOverlay = function(map, kmlOverlayId, kmlOverlayOptions) {
    BaseClass.apply(this);

    var self = this;
    self._overlays = [];
    //self.set("visible", kmlOverlayOptions.visible === undefined ? true : kmlOverlayOptions.visible);
    //self.set("zIndex", kmlOverlayOptions.zIndex || 0);
    kmlOverlayOptions.animation = kmlOverlayOptions.animation === undefined ? true : kmlOverlayOptions.animation;
    kmlOverlayOptions.preserveViewport = kmlOverlayOptions.preserveViewport  === true;
    Object.defineProperty(self, "id", {
        value: kmlOverlayId,
        writable: false
    });
    Object.defineProperty(self, "type", {
        value: "KmlOverlay",
        writable: false
    });
    Object.defineProperty(self, "map", {
        value: map,
        writable: false
    });
    var ignores = ["map", "id", "type"];
    for (var key in kmlOverlayOptions) {
        if (ignores.indexOf(key) === -1) {
            self.set(key, kmlOverlayOptions[key]);
        }
    }
};

utils.extend(KmlOverlay, BaseClass);

KmlOverlay.prototype.getOverlays = function() {
    return this._overlays;
};
KmlOverlay.prototype.getMap = function() {
    return this.map;
};
KmlOverlay.prototype.getId = function() {
    return this.id;
};

module.exports = KmlOverlay;
