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

(function($) {
    jQuery.fn.extend({
        html5_qrcode: function(qrcodeSuccess, qrcodeError, videoError) {
            return this.each(function() {
                var currentElem = $(this);

                var height = currentElem.height();
                var width = currentElem.width();

                if (height == null) {
                    height = 250;
                }

                if (width == null) {
                    width = 300;
                }

                var vidElem = $('<video width="' + width + 'px" height="' + height + 'px"></video>').appendTo(currentElem);
                var canvasElem = $('<canvas id="qr-canvas" width="' + (width - 2) + 'px" height="' + (height - 2) + 'px" style="display:none;"></canvas>').appendTo(currentElem);

                var video = vidElem[0];
                var canvas = canvasElem[0];
                var context = canvas.getContext('2d');
                var localMediaStream;

                var scan = function() {
                    if (!currentElem.is(":visible")) {
                        return; // This stops the scan
                    }
                    if (localMediaStream) {
                        context.drawImage(video, 0, 0, 307, 250);
                        try {
                            qrcodeDecoder.decode();
                        } catch (e) {
                            qrcodeError(e, localMediaStream);
                        }
                        $.data(currentElem[0], "timeout", setTimeout(scan, 500));
                    } else {
                        $.data(currentElem[0], "timeout", setTimeout(scan, 500));
                    }
                };//end snapshot function

                window.URL = window.URL || window.webkitURL || window.mozURL || window.msURL;
                navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;

                var successCallback = function(stream) {
                    try {
                        video.src = (window.URL && window.URL.createObjectURL(stream)) || stream;
                        localMediaStream = stream;
                        $.data(currentElem[0], "stream", stream);

                        video.play();
                        $.data(currentElem[0], "timeout", setTimeout(scan, 1000));
                    } catch(e) {
                        NRS.logException(e);
                    }
                };

                // Call the getUserMedia method with our callback functions
                if (navigator.getUserMedia) {
                    navigator.getUserMedia({video: true}, successCallback, function(error) {
                        videoError(error, localMediaStream);
                    });
                } else {
                    videoError('Native web camera streaming (getUserMedia) not supported in this browser.');
                }

                qrcode.callback = function (result) {
                    qrcodeSuccess(result, localMediaStream);
                    $("#qr-canvas").remove(); // So that it won't save the result of the scan
                };
            }); // end of html5_qrcode
        },
        html5_qrcode_stop: function() {
            return this.each(function() {
                //stop the stream
                var currentElem = $(this);
                var stream = $.data(currentElem[0], 'stream');
                if (stream) {
                    var tracks = stream.getVideoTracks();
                    if (tracks) {
                        for (var i=0; i<tracks.length; i++) {
                            tracks[i].stop();
                        }
                    }
                }
            });
        }
    });
})(jQuery);