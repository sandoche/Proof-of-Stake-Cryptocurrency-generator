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
 * JS initialization and core functions for API test servlet
 *
 * @depends {3rdparty/jquery.js}
 * @depends {3rdparty/bootstrap.js}
 * @depends {3rdparty/highlight.pack.js}
 */

var ATS = (function(ATS, $, undefined) {
    ATS.apiCalls = [];
    ATS.selectedApiCalls = [];

    ATS.init = function() {
        hljs.initHighlightingOnLoad();
        
        ATS.selectedApiCalls = ATS.setSelectedApiCalls();
        ATS.selectedApiCallsChange();

        $('#search').keyup(function(e) {
            if (e.keyCode == 13) {
                ATS.performSearch($(this).val());
            }
        });

        $(".collapse-link").click(function(event) {
            event.preventDefault();    
        });
        
        $("#navi-show-fields").click(function(e) {
            if ($(this).attr("data-navi-val") == "ALL") {
                $('.api-call-input-tr').each(function() {
                    if($(this).find("input").val() != "") {
                        $(this).show();
                    } else {
                        $(this).hide();
                    }
                });
                $(this).attr("data-navi-val", "NONEMPTY");
                $(this).text("Show All Fields");
            } else {
                $('.api-call-input-tr').show();
                $(this).attr("data-navi-val", "ALL");
                $(this).text("Show Non-Empty Fields");
            }
            e.preventDefault();
        });

        $("#navi-show-tabs").click(function(e) {
            if ($(this).attr("data-navi-val") == "ALL") {
                $('.api-call-All').each(function() {
                    if($(this).find('.panel-collapse.in').length != 0) {
                        $(this).show();
                    } else {
                        $(this).hide();
                    }
                });
                $(this).attr("data-navi-val", "OPEN");
                $(this).text("Show All Tabs");
            } else {
                $('.api-call-All').show();
                $(this).attr("data-navi-val", "ALL");
                $(this).text("Show Open Tabs");
            }
            e.preventDefault();
        });

        $('.api-call-sel-ALL').change(function() {
            if($(this).is(":checked")) {
                ATS.addToSelected($(this));
            } else {
                ATS.removeFromSelected($(this));
            }
        });

        $('#navi-select-all-d-add-btn').click(function(e) {
            $.each($('.api-call-sel-ALL:visible:not(:checked)'), function(key, value) {
                ATS.addToSelected($(value));
            });
            e.preventDefault();
        });

        $('#navi-select-all-d-replace-btn').click(function(e) {
            ATS.selectedApiCalls = [];
            $.each($('.api-call-sel-ALL:visible'), function(key, value) {
                ATS.addToSelected($(value));
            });
            e.preventDefault();
        });

        $('#navi-deselect-all-d-btn').click(function(e) {
            $.each($('.api-call-sel-ALL:visible'), function(key, value) {
                ATS.removeFromSelected($(value));
            });
            e.preventDefault();
        });

        $('#navi-deselect-all-btn').click(function(e) {
            ATS.selectedApiCalls = [];
            $('.api-call-sel-ALL').prop('checked', false);
            ATS.selectedApiCallsChange();
            e.preventDefault();
        });

    };

    ATS.performSearch = function(searchStr) {
        if (searchStr == '') {
            $('.api-call-All').show();
        } else {
            $('.api-call-All').hide();
            $('.topic-link').css('font-weight', 'normal');
            for(var i=0; i<ATS.apiCalls.length; i++) {
                var apiCall = ATS.apiCalls[i];
                if (new RegExp(searchStr.toLowerCase()).test(apiCall.toLowerCase())) {
                    $('#api-call-' + apiCall).show();
                }
            }
        }
    };

    ATS.submitForm = function(form, fileParameter) {
        var url = $('#formAction').val();
        var params = {};
        for (var i = 0; i < form.elements.length; i++) {
            if (form.elements[i].type != 'button' && form.elements[i].value && form.elements[i].value != 'submit') {
                var key = form.elements[i].name;
                var value = form.elements[i].value;
                if (key in params) {
                    var index = params[key].length;
                    params[key][index] = value;
                } else {
                    params[key] = [value];
                }
            }
        }
        if (params["requestType"] == "downloadJPLSnapshot" || params["requestType"] == "downloadPrunableMessage" || params["requestType"] == "downloadTaggedData") {
            return true;
        }
        var contentType;
        var processData;
        var formData = null;
        var uploadField;
        if (form.encoding == "multipart/form-data") {
            uploadField = $('#' + fileParameter + params["requestType"]);
        }
        /*
        if (params["requestType"] == "downloadPrunableMessage" || params["requestType"] == "downloadTaggedData") {
            url += "?";
            for (key in params) {
                if (!params.hasOwnProperty(key)) {
                    continue;
                }
                url += encodeURIComponent(key) + "=" + encodeURIComponent(params[key]) + "&";
            }
            window.location = url;
            return false;
        } else
        */
        if (uploadField) {
            // inspired by http://stackoverflow.com/questions/5392344/sending-multipart-formdata-with-jquery-ajax
            contentType = false;
            processData = false;
            // TODO works only for new browsers
            formData = new FormData();
            for (key in params) {
                if (!params.hasOwnProperty(key)) {
                    continue;
                }
                if (key == fileParameter) {
                    continue;
                }
                formData.append(key, params[key]);
            }
            var file = uploadField[0].files[0];
            if (file) {
                formData.append(fileParameter, file);
                if (!formData["filename"]) {
                    formData.append("filename", file.name);
                }
            }
        } else {
            // JQuery defaults
            contentType = "application/x-www-form-urlencoded; charset=UTF-8";
            processData = true;
        }
        url += "?requestType=" + params["requestType"];
        
        $.ajax({
            url: url,
            type: 'POST',
            data: (formData != null ? formData : params),
            traditional: true, // "true" needed for duplicate params
            contentType: contentType,
            processData: processData
        })
        .done(function(result) {
            var resultStr = JSON.stringify(JSON.parse(result), null, 4);
            var code_elem = form.getElementsByClassName("result")[0];
            code_elem.textContent = resultStr;
            hljs.highlightBlock(code_elem);
        })
        .error(function() {
            alert('API not available, check if Nxt Server is running!');
        });
        if ($(form).has('.uri-link').length > 0) { 
            var uri = '/nxt?' + jQuery.param(params, true);
            form.getElementsByClassName("uri-link")[0].innerHTML = '<a href="' + uri + '" target="_blank" style="font-size:12px;font-weight:normal;">Open GET URL</a>';
        }
        return false;
    };

    ATS.selectedApiCallsChange = function() {
        var newUrl = '/test?requestTypes=' + encodeURIComponent(ATS.selectedApiCalls.join('_'));
        var navi = $('#navi-selected');
        navi.attr('href', newUrl);
        navi.text('SELECTED (' + ATS.selectedApiCalls.length + ')');
        ATS.setCookie('selected_api_calls', ATS.selectedApiCalls.join('_'), 30);
    };

    ATS.setSelectedApiCalls = function() {
        var calls = [];
        var getParam = ATS.getUrlParameter('requestTypes');
        var cookieParam = ATS.getCookie('selected_api_calls');
        if (getParam != undefined && getParam != '') {
            calls=getParam.split('_');
        } else if (cookieParam != undefined && cookieParam != ''){
            calls=cookieParam.split('_');
        }
        for (var i=0; i<calls.length; i++) {
            $('#api-call-sel-' + calls[i]).prop('checked', true);
        }
        return calls;
    };
    
    ATS.addToSelected = function(elem) {
        var type=elem.attr('id').substr(13);
        elem.prop('checked', true);
        if($.inArray(type, ATS.selectedApiCalls) == -1) {
            ATS.selectedApiCalls.push(type);
            ATS.selectedApiCallsChange();
        }
    };
    
    ATS.removeFromSelected = function(elem) {
        var type=elem.attr('id').substr(13);
        elem.prop('checked', false);
        var index = ATS.selectedApiCalls.indexOf(type);
        if (index > -1) {
            ATS.selectedApiCalls.splice(index, 1);
            ATS.selectedApiCallsChange();
        }
    };

    return ATS;
}(ATS || {}, jQuery));

$(document).ready(function() {
    ATS.init();
});