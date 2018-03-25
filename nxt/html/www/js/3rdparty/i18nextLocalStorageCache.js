(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define('i18nextLocalStorageCache', factory) :
  (global.i18nextLocalStorageCache = factory());
}(this, function () { 'use strict';

  var babelHelpers = {};

  babelHelpers.classCallCheck = function (instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  };

  babelHelpers.createClass = function () {
    function defineProperties(target, props) {
      for (var i = 0; i < props.length; i++) {
        var descriptor = props[i];
        descriptor.enumerable = descriptor.enumerable || false;
        descriptor.configurable = true;
        if ("value" in descriptor) descriptor.writable = true;
        Object.defineProperty(target, descriptor.key, descriptor);
      }
    }

    return function (Constructor, protoProps, staticProps) {
      if (protoProps) defineProperties(Constructor.prototype, protoProps);
      if (staticProps) defineProperties(Constructor, staticProps);
      return Constructor;
    };
  }();

  babelHelpers;

  var arr = [];
  var each = arr.forEach;
  var slice = arr.slice;

  function defaults(obj) {
    each.call(slice.call(arguments, 1), function (source) {
      if (source) {
        for (var prop in source) {
          if (obj[prop] === undefined) obj[prop] = source[prop];
        }
      }
    });
    return obj;
  }

  function debounce(func, wait, immediate) {
    var timeout;
    return function () {
      var context = this,
          args = arguments;
      var later = function later() {
        timeout = null;
        if (!immediate) func.apply(context, args);
      };
      var callNow = immediate && !timeout;
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
      if (callNow) func.apply(context, args);
    };
  };

  var storage = {
    setItem: function setItem(key, value) {
      if (window.localStorage) {
        try {
          window.localStorage.setItem(key, value);
        } catch (e) {
          //f.log('failed to set value for key "' + key + '" to localStorage.');
        }
      }
    },
    getItem: function getItem(key, value) {
      if (window.localStorage) {
        try {
          return window.localStorage.getItem(key, value);
        } catch (e) {
          //f.log('failed to get value for key "' + key + '" from localStorage.');
          return undefined;
        }
      }
    }
  };

  function getDefaults() {
    return {
      enabled: false,
      prefix: 'i18next_res_',
      expirationTime: 7 * 24 * 60 * 60 * 1000
    };
  }

  var Cache = function () {
    function Cache(services) {
      var options = arguments.length <= 1 || arguments[1] === undefined ? {} : arguments[1];
      babelHelpers.classCallCheck(this, Cache);

      this.init(services, options);

      this.type = 'cache';
      this.debouncedStore = debounce(this.store, 10000);
    }

    babelHelpers.createClass(Cache, [{
      key: 'init',
      value: function init(services) {
        var options = arguments.length <= 1 || arguments[1] === undefined ? {} : arguments[1];

        this.services = services;
        this.options = defaults(options, this.options || {}, getDefaults());
      }
    }, {
      key: 'load',
      value: function load(lngs, callback) {
        var _this = this;

        var store = {},
            nowMS = new Date().getTime();

        if (window.localStorage) {
          (function () {
            var todo = lngs.length;

            lngs.forEach(function (lng) {
              var local = storage.getItem(_this.options.prefix + lng);

              if (local) {
                local = JSON.parse(local);
                if (local.i18nStamp && local.i18nStamp + _this.options.expirationTime > nowMS) {
                  store[lng] = local;
                }
              }

              todo--;
              if (todo === 0) callback(null, store);
            });
          })();
        }
      }
    }, {
      key: 'store',
      value: function store(_store) {
        if (window.localStorage) {
          for (var m in _store) {
            _store[m].i18nStamp = new Date().getTime();
            storage.setItem(this.options.prefix + m, JSON.stringify(_store[m]));
          }
        }
        return;
      }
    }, {
      key: 'save',
      value: function save(store) {
        this.debouncedStore(store);
        return;
      }
    }]);
    return Cache;
  }();

  Cache.type = 'cache';

  return Cache;

}));