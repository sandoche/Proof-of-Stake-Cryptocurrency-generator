/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of the Nxt software, including this file, may be copied, modified, *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

/**
 * @depends {3rdparty/jquery-2.1.0.js}
 */
var NRS = (function(NRS, $, undefined) {

    /**
     * Usage:
     *
     * 1. Declare the template like this:
     *      <div id="my_page_name_page" class="paginated page">
     *          <script type="text/x-handlebars-template">
     *              {{ property }}
     *          </script>
     *      </div>
     *
     * 2. Get the view instance in page initializer:
     *      NRS.pages.my_page_name = function () {
     *          var view = NRS.simpleview.get('my_page_name_page', {
     *              // optionally provide initial model data
     *              // all properties on the view will be available in the template
     *              property: 'Hello'
     *          });
     *      }
     *
     * 3. Creating the view instance will immediately render the view contents
     *    so make sure it is called when the DOM is available.
     *
     * 4. Later on you can manually render the view through view.render() model
     *    data can either be set directly on the view:
     *
     *      view.property = 'Bye bye';
     *
     *    And then rendered:
     *
     *      view.render();
     *
     *    Or you can pass only the model data that changed to the render function 
     *    directly, the object passed to render will be applied on the view with 
     *    jquery $.extend function.
     *
     *      view.render({ property: 'Good day' });
     **/    

    function SimpleView(element_id, template, scope) {
        this.__element_id = element_id;
        this.__template   = template;
        $.extend(this, scope||{});
        this.render();
    }
    SimpleView.prototype = {
        render: function (scope) {
            $.extend(this, scope||{});
            $('#'+this.__element_id).empty().html(this.__template(this));
        }
    };

    /* Compiled HandleBars templates */
    var templates = {};

    NRS.simpleview = {

        /* Contains callback closures for use with HandleBars 'callback' helper */
        callbacks: {},

        /* @returns SimpleView */
        get: function (element_id, scope) {
            if (!templates[element_id]) {
                var template_el  = $('#'+element_id+' > script');
                if (template_el.length == 0) {
                    throw new Error('Missing '+element_id+' template, make sure it exists');
                }
                templates[element_id] = Handlebars.compile(template_el.html());
                $('#'+element_id).empty();
            }
            // clear out all callbacks for the previous rendering
            NRS.simpleview.callbacks[element_id] = [];
            return new SimpleView(element_id, templates[element_id], scope);
        }
    };

    /**
     * For use in templates, renders a string if condition is true. Accepts
     * multiple arguments, results are concatenated by a single space.
     *
     * Usage:
     *
     *      {{when true '1' false '2' true '3' }}
     *      Will render as: "1 3"
     *
     * Both the condition and string content can be model objects (properties 
     * on SimpleView).
     */
    Handlebars.registerHelper('when', function() {
        var result = [];
        for (var i=0; i<arguments.length; i++) {
            if (arguments[i++]) {
                result.push(arguments[i]);
            }
        }
        return new Handlebars.SafeString(result.join(' '));
    });

    Handlebars.registerHelper('callback', function(view, fn) {
        var view_id = view.__element_id;
        if (!view_id) {
            throw new Error("Argument is not a SimpleView");
        }
        if (typeof fn != 'function') {
            throw new Error("Argument is not a function");
        }
        var callbacks = NRS.simpleview.callbacks[view_id];
        var index = callbacks.length, self = this;
        callbacks.push(function (element) {
            fn.call(view, self, element);
        });
        return 'NRS.simpleview.callbacks.'+view_id+'['+index+'].call(null, $(this))';
    });

    Handlebars.registerHelper('i18n',
        function (str) {
            return $.t(str);
        }
    );

    return NRS;

})(NRS || {}, jQuery);