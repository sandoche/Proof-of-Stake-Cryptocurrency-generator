/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2020 Jelurida IP B.V.                                     *
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
 * @depends {nrs.js}
 */
var NRS = (function (NRS, $) {
	var _password;
	var _decryptionPassword;
	var _decryptedTransactions;
	var _encryptedNote;

	NRS.resetEncryptionState = function () {
		_password = null;
		_decryptionPassword = null;
		_decryptedTransactions = {};
		_encryptedNote = null;
	};
	NRS.resetEncryptionState();

	NRS.generatePublicKey = function(secretPhrase) {
		if (!secretPhrase) {
			if (NRS.rememberPassword) {
				secretPhrase = _password;
			} else {
				throw { message: $.t("error_generate_public_key_no_password") };
			}
		}

		return NRS.getPublicKey(converters.stringToHexString(secretPhrase));
	};

	NRS.getPublicKey = function(id, isAccountId) {
		if (isAccountId) {
            var publicKey = "";
			NRS.sendRequest("getAccountPublicKey", {
				"account": id
			}, function(response) {
				if (!response.publicKey) {
					throw $.t("error_no_public_key");
				} else {
					publicKey = response.publicKey;
				}
			}, { isAsync: false }); //synchronous!
            return publicKey;
		} else {
			var secretPhraseBytes = converters.hexStringToByteArray(id);
			var digest = simpleHash(secretPhraseBytes);
			return converters.byteArrayToHexString(curve25519.keygen(digest).p);
		}
	};

	NRS.getPrivateKey = function(secretPhrase) {
		var bytes = simpleHash(converters.stringToByteArray(secretPhrase));
        return converters.shortArrayToHexString(curve25519_clamp(converters.byteArrayToShortArray(bytes)));
	};

	NRS.getAccountId = function(secretPhrase, isRsFormat) {
		return NRS.getAccountIdFromPublicKey(NRS.getPublicKey(converters.stringToHexString(secretPhrase)), isRsFormat);
	};

	NRS.getAccountIdFromPublicKey = function(publicKey, isRsFormat) {
		var hex = converters.hexStringToByteArray(publicKey);
		var account = simpleHash(hex);
		account = converters.byteArrayToHexString(account);
		var slice = (converters.hexStringToByteArray(account)).slice(0, 8);
		var accountId = byteArrayToBigInteger(slice).toString();
		if (isRsFormat) {
			return NRS.convertNumericToRSAccountFormat(accountId);
		} else {
			return accountId;
		}
	};

	NRS.getEncryptionKeys = function (options, secretPhrase){
		if (!options.sharedKey) {
			if (!options.privateKey) {
				if (!secretPhrase) {
					if (NRS.rememberPassword) {
						secretPhrase = _password;
					} else {
						throw {
							"message": $.t("error_encryption_passphrase_required"),
							"errorCode": 1
						};
					}
				}

				options.privateKey = converters.hexStringToByteArray(NRS.getPrivateKey(secretPhrase));
			}

			if (!options.publicKey) {
				if (!options.account) {
					throw {
						"message": $.t("error_account_id_not_specified"),
						"errorCode": 2
					};
				}

				try {
					options.publicKey = converters.hexStringToByteArray(NRS.getPublicKey(options.account, true));
				} catch (err) {
					var nxtAddress = new NxtAddress();

					if (!nxtAddress.set(options.account)) {
						throw {
							"message": $.t("error_invalid_account_id"),
							"errorCode": 3
						};
					} else {
						throw {
							"message": $.t("error_public_key_not_specified"),
							"errorCode": 4
						};
					}
				}
			} else if (typeof options.publicKey == "string") {
				options.publicKey = converters.hexStringToByteArray(options.publicKey);
			}
		}
		return options;
	};

    NRS.encryptNote = function(message, options, secretPhrase) {
		try {
			options = NRS.getEncryptionKeys(options, secretPhrase);
			var encrypted = encryptData(converters.stringToByteArray(message), options);
			return {
				"message": converters.byteArrayToHexString(encrypted.data),
				"nonce": converters.byteArrayToHexString(encrypted.nonce)
			};
		} catch (err) {
			if (err.errorCode && err.errorCode < 5) {
				throw err;
			} else {
				throw {
					"message": $.t("error_message_encryption"),
					"errorCode": 5
				};
			}
		}
	};

	NRS.decryptNote = function(message, options, secretPhrase) {
		try {
			if (!options.sharedKey) {
				if (!options.privateKey) {
					if (!secretPhrase) {
						if (NRS.rememberPassword) {
							secretPhrase = _password;
						} else if (_decryptionPassword) {
							secretPhrase = _decryptionPassword;
						} else {
							throw {
								"message": $.t("error_decryption_passphrase_required"),
								"errorCode": 1
							};
						}
					}

					options.privateKey = converters.hexStringToByteArray(NRS.getPrivateKey(secretPhrase));
				}

				if (!options.publicKey) {
					if (!options.account) {
						throw {
							"message": $.t("error_account_id_not_specified"),
							"errorCode": 2
						};
					}

					options.publicKey = converters.hexStringToByteArray(NRS.getPublicKey(options.account, true));
				}
			}
			if (options.nonce) {
				options.nonce = converters.hexStringToByteArray(options.nonce);
			}
			return decryptData(converters.hexStringToByteArray(message), options);
		} catch (err) {
			if (err.errorCode && err.errorCode < 3) {
				throw err;
			} else {
			    NRS.logConsole(err.message);
				throw {
					"message": $.t("error_message_decryption"),
					"errorCode": 3
				};
			}
		}
	};

	NRS.signBytes = function(message, secretPhrase) {
		if (!secretPhrase) {
			if (NRS.rememberPassword) {
				secretPhrase = _password;
			} else {
				throw {
					"message": $.t("error_signing_passphrase_required"),
					"errorCode": 1
				};
			}
		}
		var messageBytes = converters.hexStringToByteArray(message);
		var secretPhraseBytes = converters.hexStringToByteArray(secretPhrase);

        var digest = simpleHash(secretPhraseBytes);
        var s = curve25519.keygen(digest).s;
        var m = simpleHash(messageBytes);
        var x = simpleHash(m, s);
        var y = curve25519.keygen(x).p;
        var h = simpleHash(m, y);
        var v = curve25519.sign(h, x, s);
		return converters.byteArrayToHexString(v.concat(h));
    };

	NRS.verifySignature = function(signature, message, publicKey, callback) {
		var signatureBytes = converters.hexStringToByteArray(signature);
		var messageBytes = converters.hexStringToByteArray(message);
		var publicKeyBytes = converters.hexStringToByteArray(publicKey);
		var v = signatureBytes.slice(0, 32);
		var h = signatureBytes.slice(32);
		var y = curve25519.verify(v, h, publicKeyBytes);
		var m = simpleHash(messageBytes);
		var h2 = simpleHash(m, y);
		if (!areByteArraysEqual(h, h2)) {
            callback({
                "errorCode": 1,
                "errorDescription": $.t("error_signature_verification_client")
            }, message);
            return false;
        }
        return true;
	};

	NRS.setEncryptionPassword = function(password) {
		_password = password;
	};

	NRS.setDecryptionPassword = function(password) {
		_decryptionPassword = password;
	};

	NRS.tryToDecryptMessage = function(message) {
		if (_decryptedTransactions && _decryptedTransactions[message.transaction]) {
			if (_decryptedTransactions[message.transaction].encryptedMessage) {
				return _decryptedTransactions[message.transaction].encryptedMessage; // cache is saved differently by the info modal vs the messages table
			}
		}
		try {
			if (!message.attachment.encryptedMessage.data) {
				return { message: $.t("message_empty") };
			} else {
				var decoded = NRS.decryptNote(message.attachment.encryptedMessage.data, {
					"nonce": message.attachment.encryptedMessage.nonce,
					"account": (message.recipient == NRS.account ? message.sender : message.recipient),
					"isText": message.attachment.encryptedMessage.isText,
					"isCompressed": message.attachment.encryptedMessage.isCompressed
				});
			}
			return decoded;
		} catch (err) {
			throw err;
		}
	};

	NRS.tryToDecrypt = function(transaction, fields, account, options) {
		var showDecryptionForm = false;
		if (!options) {
			options = {};
		}
		var nrFields = Object.keys(fields).length;
		var formEl = (options.formEl ? NRS.escapeRespStr(options.formEl) : "#transaction_info_output_bottom");
		var outputEl = (options.outputEl ? NRS.escapeRespStr(options.outputEl) : "#transaction_info_output_bottom");
		var output = "";
		var identifier = (options.identifier ? transaction[options.identifier] : transaction.transaction);

		//check in cache first..
		if (_decryptedTransactions && _decryptedTransactions[identifier]) {
			var decryptedTransaction = _decryptedTransactions[identifier];
			$.each(fields, function(key, title) {
				if (typeof title != "string") {
					title = title.title;
				}
				if (key in decryptedTransaction) {
                    output += formatMessageArea(title, nrFields, decryptedTransaction[key], options, transaction);
				} else {
					//if a specific key was not found, the cache is outdated..
					output = "";
					delete _decryptedTransactions[identifier];
					return false;
				}
			});
		}

		if (!output) {
			$.each(fields, function(key, title) {
				var data = {};
				var encrypted = "";
				var nonce = "";
				var nonceField = (typeof title != "string" ? title.nonce : key + "Nonce");

				if (key == "encryptedMessage" || key == "encryptToSelfMessage") {
					encrypted = transaction.attachment[key].data;
					nonce = transaction.attachment[key].nonce;
				} else if (transaction.attachment && transaction.attachment[key]) {
					encrypted = transaction.attachment[key];
					nonce = transaction.attachment[nonceField];
				} else if (transaction[key] && typeof transaction[key] == "object") {
					encrypted = transaction[key].data;
					nonce = transaction[key].nonce;
				} else if (transaction[key]) {
					encrypted = transaction[key];
					nonce = transaction[nonceField];
				} else {
					encrypted = "";
				}

				if (encrypted) {
					if (typeof title != "string") {
						title = title.title;
					}
					try {
						var decryptOptions = {};
						if (options.sharedKey) {
							decryptOptions = { "sharedKey": converters.hexStringToByteArray(options.sharedKey) }
						} else {
							decryptOptions = {
								"nonce": nonce,
								"account": account
							};
						}
						if (transaction.goodsIsText) {
                            decryptOptions.isText = transaction.goodsIsText;
						} else {
                            decryptOptions.isText = transaction.attachment[key].isText;
                            decryptOptions.isCompressed = transaction.attachment[key].isCompressed;
						}
                        data = NRS.decryptNote(encrypted, decryptOptions);
					} catch (err) {
						if (err.errorCode && err.errorCode == 1) {
							showDecryptionForm = true;
							return false;
						} else {
							if (title) {
								var translatedTitle = NRS.getTranslatedFieldName(title).toLowerCase();
								if (!translatedTitle) {
									translatedTitle = NRS.escapeRespStr(title).toLowerCase();
								}

								data.message = $.t("error_could_not_decrypt_var", {
									"var": translatedTitle
								}).capitalize();
							} else {
								data.message = $.t("error_could_not_decrypt");
							}
						}
					}
                    output += formatMessageArea(title, nrFields, data, options, transaction);
				}
			});
		}

		if (showDecryptionForm) {
			_encryptedNote = {
				"transaction": transaction,
				"fields": fields,
				"account": account,
				"options": options,
				"identifier": identifier
			};
			if (_encryptedNote.account) {
				$("#decrypt_note_secret_phrase_div").show();
                $("#decrypt_note_form_container").find(".callout").hide();
            } else {
                $("#decrypt_note_form_password").val("");
                $("#decrypt_note_secret_phrase_div").hide();
            }
			$("#decrypt_note_form_container").detach().appendTo(formEl);
			$("#decrypt_note_form_container, " + formEl).show();
		} else {
			NRS.removeDecryptionForm();
			$(outputEl).append(output).show();
		}
	};

	NRS.removeDecryptionForm = function($modal) {
		var noteFormContainer = $("#decrypt_note_form_container");
		if (($modal && $modal.find("#decrypt_note_form_container").length) || (!$modal && noteFormContainer.length)) {
			noteFormContainer.find("input").val("");
			noteFormContainer.hide().detach().appendTo("body");
		}
	};

	var decryptNoteFormContainer = $("#decrypt_note_form_container");
	decryptNoteFormContainer.find("button.btn-primary").click(function() {
		NRS.decryptNoteFormSubmit();
	});

	decryptNoteFormContainer.on("submit", function(e) {
		e.preventDefault();
		NRS.decryptNoteFormSubmit();
	});

    var formatMessageArea = function (title, nrFields, data, options, transaction) {
		var outputStyle = (!options.noPadding && title ? "padding-left:5px;" : "");
		var labelStyle = (nrFields > 1 ? " style='margin-top:5px'" : "");
		var label = (title ? "<label" + labelStyle + "><i class='fa fa-unlock'></i> " + String(title).escapeHTML() + "</label>" : "");
		var msg;
		if (NRS.isTextMessage(transaction)) {
			msg = String(data.message).autoLink().nl2br();
		} else {
			msg = $.t("binary_data");
		}
		var sharedKeyField = "";
		var downloadLink = "";
		if (data.sharedKey) {
			sharedKeyField = "<div><label>" + $.t('shared_key') + "</label><br><span>" + data.sharedKey + "</span></div><br>";
			if (!NRS.isTextMessage(transaction) && transaction.block) {
				downloadLink = NRS.getMessageDownloadLink(transaction.transaction, data.sharedKey) + "<br>";
			}
		}
        return "<div style='" + outputStyle + "'>" + label + "<div>" + msg + "</div>" + sharedKeyField + downloadLink + "</div>";
    };

    NRS.decryptNoteFormSubmit = function() {
		var $form = $("#decrypt_note_form_container");
		if (!_encryptedNote) {
			$form.find(".callout").html($.t("error_encrypted_note_not_found")).show();
			return;
		}

		var password = $form.find("input[name=secretPhrase]").val();
		var sharedKey = $form.find("input[name=sharedKey]").val();
		var useSharedKey = false;
		if (!password) {
			if (NRS.rememberPassword) {
				password = _password;
			} else if (_decryptionPassword) {
				password = _decryptionPassword;
			} else if (!sharedKey) {
				$form.find(".callout").html($.t("error_passphrase_or_shared_key_required")).show();
				return;
			}
			useSharedKey = true;
		}

		var accountId = NRS.getAccountId(password);
		if (accountId != NRS.account && !useSharedKey) {
			$form.find(".callout").html($.t("error_incorrect_passphrase")).show();
			return;
		}

		var rememberPassword = $form.find("input[name=rememberPassword]").is(":checked");
		var output = "";
		var decryptionError = false;
		var decryptedFields = {};
		var nrFields = Object.keys(_encryptedNote.fields).length;

		$.each(_encryptedNote.fields, function(key, title) {
			var data = {};
			var encrypted = "";
			var nonce = "";
			var nonceField = (typeof title != "string" ? title.nonce : key + "Nonce");
			if (key == "encryptedMessage" || key == "encryptToSelfMessage") {
                var otherAccount = _encryptedNote.account;
			    if (key == "encryptToSelfMessage") {
					otherAccount = accountId;
				}
				encrypted = _encryptedNote.transaction.attachment[key].data;
				nonce = _encryptedNote.transaction.attachment[key].nonce;
			} else if (_encryptedNote.transaction.attachment && _encryptedNote.transaction.attachment[key]) {
				encrypted = _encryptedNote.transaction.attachment[key];
				nonce = _encryptedNote.transaction.attachment[nonceField];
			} else if (_encryptedNote.transaction[key] && typeof _encryptedNote.transaction[key] == "object") {
				encrypted = _encryptedNote.transaction[key].data;
				nonce = _encryptedNote.transaction[key].nonce;
			} else if (_encryptedNote.transaction[key]) {
				encrypted = _encryptedNote.transaction[key];
				nonce = _encryptedNote.transaction[nonceField];
			} else {
				encrypted = "";
			}

			if (encrypted) {
				if (typeof title != "string") {
					title = title.title;
				}
				try {
					var options = {};
					if (useSharedKey) {
						options.sharedKey = converters.hexStringToByteArray(sharedKey);
					} else {
						options.nonce = nonce;
						options.account = otherAccount;
                    }
                    if (_encryptedNote.transaction.goodsIsText) {
                        options.isText = _encryptedNote.transaction.goodsIsText;
                    } else {
                        options.isText = _encryptedNote.transaction.attachment[key].isText;
                        options.isCompressed = _encryptedNote.transaction.attachment[key].isCompressed;
                    }
                    data = NRS.decryptNote(encrypted, options, password);
					decryptedFields[key] = data;
				} catch (err) {
					if (useSharedKey) {
						data = { message: $.t("error_could_not_decrypt_message") };
						decryptedFields[key] = data;
					} else {
						decryptionError = true;
						var message = String(err.message ? err.message : err);
						$form.find(".callout").html(message.escapeHTML());
						return false;
					}
				}
                output += formatMessageArea(title, nrFields, data, _encryptedNote.options, _encryptedNote.transaction);
			}
		});
		if (decryptionError) {
			return;
		}
		_decryptedTransactions[_encryptedNote.identifier] = decryptedFields;

		//only save 150 decrypted messages in cache...
		var decryptionKeys = Object.keys(_decryptedTransactions);
		if (decryptionKeys.length > 150) {
			delete _decryptedTransactions[decryptionKeys[0]];
		}
		NRS.removeDecryptionForm();
		var outputEl = (_encryptedNote.options.outputEl ? NRS.escapeRespStr(_encryptedNote.options.outputEl) : "#transaction_info_output_bottom");
		$(outputEl).append(output).show();
		_encryptedNote = null;
		if (rememberPassword) {
			_decryptionPassword = password;
		}
	};

	NRS.decryptAllMessages = function(messages, password, sharedKey) {
		var useSharedKey = false;
		if (!password) {
			if (!sharedKey) {
				throw {
					"message": $.t("error_passphrase_required"),
					"errorCode": 1
				};
			}
			useSharedKey = true;
		} else {
			var accountId = NRS.getAccountId(password);
			if (accountId != NRS.account) {
				throw {
					"message": $.t("error_incorrect_passphrase"),
					"errorCode": 2
				};
			}
		}

		var success = 0;
		var error = 0;
		for (var i = 0; i < messages.length; i++) {
			var message = messages[i];
			if (message.attachment.encryptedMessage && !_decryptedTransactions[message.transaction]) {
				try {
					var otherUser = (message.sender == NRS.account ? message.recipient : message.sender);
					var options = {};
					if (useSharedKey) {
						options.sharedKey = converters.hexStringToByteArray(sharedKey);
					} else {
						options.nonce = message.attachment.encryptedMessage.nonce;
						options.account = otherUser;
                    }
                    if (_encryptedNote.transaction.goodsIsText) {
                        options.isText = message.goodsIsText;
                    } else {
                        options.isText = message.attachment.encryptedMessage.isText;
                        options.isCompressed = message.attachment.encryptedMessage.isCompressed;
                    }
                    var decoded = NRS.decryptNote(message.attachment.encryptedMessage.data, options, password);
					_decryptedTransactions[message.transaction] = {
						encryptedMessage: decoded
					};
					success++;
				} catch (err) {
					if (!useSharedKey) {
						_decryptedTransactions[message.transaction] = {
							"message": $.t("error_decryption_unknown")
						};
					}
					error++;
				}
			}
		}

		//noinspection RedundantIfStatementJS
		if (success || !error) {
			return true;
		} else {
			return false;
		}
	};

	function simpleHash(b1, b2) {
		var sha256 = CryptoJS.algo.SHA256.create();
		sha256.update(converters.byteArrayToWordArray(b1));
		if (b2) {
			sha256.update(converters.byteArrayToWordArray(b2));
		}
		var hash = sha256.finalize();
		return converters.wordArrayToByteArrayImpl(hash, false);
	}

	function areByteArraysEqual(bytes1, bytes2) {
		if (bytes1.length !== bytes2.length) {
            return false;
        }
		for (var i = 0; i < bytes1.length; ++i) {
			if (bytes1[i] !== bytes2[i]) {
                return false;
            }
		}
		return true;
	}

	function curve25519_clamp(curve) {
		curve[0] &= 0xFFF8;
		curve[15] &= 0x7FFF;
		curve[15] |= 0x4000;
		return curve;
	}

	function byteArrayToBigInteger(byteArray) {
		var value = new BigInteger("0", 10);
		var temp1, temp2;
		for (var i = byteArray.length - 1; i >= 0; i--) {
			temp1 = value.multiply(new BigInteger("256", 10));
			temp2 = temp1.add(new BigInteger(byteArray[i].toString(10), 10));
			value = temp2;
		}
		return value;
	}

	function aesEncrypt(payload, options) {
        var ivBytes = getRandomBytes(16);

		// CryptoJS likes WordArray parameters
		var wordArrayPayload = converters.byteArrayToWordArray(payload);
		var sharedKey;
		if (!options.sharedKey) {
			sharedKey = getSharedSecret(options.privateKey, options.publicKey);
		} else {
			sharedKey = options.sharedKey.slice(0); //clone
		}
        if (options.nonce !== undefined) {
            for (var i = 0; i < 32; i++) {
                sharedKey[i] ^= options.nonce[i];
            }
        }
		var key = CryptoJS.SHA256(converters.byteArrayToWordArray(sharedKey));
		var encrypted = CryptoJS.AES.encrypt(wordArrayPayload, key, {
			iv: converters.byteArrayToWordArray(ivBytes)
		});
		var ivOut = converters.wordArrayToByteArray(encrypted.iv);
		var ciphertextOut = converters.wordArrayToByteArray(encrypted.ciphertext);
		return ivOut.concat(ciphertextOut);
	}

	NRS.aesEncrypt = function(plaintext, sharedKey) {
	    return aesEncrypt(converters.stringToByteArray(plaintext), {sharedKey: converters.stringToByteArray(sharedKey)});
	};

	function aesDecrypt(ivCiphertext, options) {
		if (ivCiphertext.length < 16 || ivCiphertext.length % 16 != 0) {
			throw {
				name: "invalid ciphertext"
			};
		}

		var iv = converters.byteArrayToWordArray(ivCiphertext.slice(0, 16));
		var ciphertext = converters.byteArrayToWordArray(ivCiphertext.slice(16));

		// shared key is use for two different purposes here
		// (1) if nonce exists, shared key represents the shared secret between the private and public keys
		// (2) if nonce does not exists, shared key is the specific key needed for decryption already xored
		// with the nonce and hashed
		var sharedKey;
		if (!options.sharedKey) {
			sharedKey = getSharedSecret(options.privateKey, options.publicKey);
		} else {
			sharedKey = options.sharedKey.slice(0); //clone
		}

		var key;
		if (options.nonce) {
			for (var i = 0; i < 32; i++) {
				sharedKey[i] ^= options.nonce[i];
			}
			key = CryptoJS.SHA256(converters.byteArrayToWordArray(sharedKey));
		} else {
			key = converters.byteArrayToWordArray(sharedKey);
		}

		var encrypted = CryptoJS.lib.CipherParams.create({
			ciphertext: ciphertext,
			iv: iv,
			key: key
		});

		var decrypted = CryptoJS.AES.decrypt(encrypted, key, {
			iv: iv
		});

		return {
            decrypted: converters.wordArrayToByteArray(decrypted),
            sharedKey: converters.wordArrayToByteArray(key)
		};
	}

    NRS.encryptDataRoof = function(data, options) {
   		return encryptData(data, options);
   	};

    function encryptData(plaintext, options) {
        options.nonce = getRandomBytes(32);
        if (!options.sharedKey) {
            options.sharedKey = getSharedSecret(options.privateKey, options.publicKey);
        }
        var compressedPlaintext = pako.gzip(new Uint8Array(plaintext));
		var data = aesEncrypt(compressedPlaintext, options);
		return {
			"nonce": options.nonce,
			"data": data
		};
	}

	NRS.decryptDataRoof = function(data, options) {
		return decryptData(data, options);
	};

	function decryptData(data, options) {
		if (!options.sharedKey) {
			options.sharedKey = getSharedSecret(options.privateKey, options.publicKey);
		}

		var result = aesDecrypt(data, options);
		var binData = new Uint8Array(result.decrypted);
		if (!(options.isCompressed === false)) {
			binData = pako.inflate(binData);
		}
		var message;
		if (!(options.isText === false)) {
			message = converters.byteArrayToString(binData);
		} else {
			message = converters.byteArrayToHexString(binData);
		}
        return { message: message, sharedKey: converters.byteArrayToHexString(result.sharedKey) };
	}

	function getSharedSecret(key1, key2) {
		return converters.shortArrayToByteArray(curve25519_(converters.byteArrayToShortArray(key1), converters.byteArrayToShortArray(key2), null));
	}

    NRS.sharedSecretToSharedKey = function (sharedSecret, nonce) {
        for (var i = 0; i < 32; i++) {
            sharedSecret[i] ^= nonce[i];
        }
        return simpleHash(sharedSecret);
    };

    NRS.getSharedKey = function (privateKey, publicKey, nonce) {
		var sharedSecret = getSharedSecret(privateKey, publicKey);
        return NRS.sharedSecretToSharedKey(sharedSecret, nonce);
	};

	NRS.encryptFile = function(file, options, callback) {
		var r;
		try {
			r = new FileReader();
		} catch(e) {
			throw $.t("encrypted_file_upload_not_supported");
		}
		r.onload = function (e) {
			var bytes = e.target.result;
			options.isText = false;
			var encrypted = encryptData(bytes, options);
			var blobData = Uint8Array.from(encrypted.data);
			var blob = new Blob([ blobData ], { type: "application/octet-stream" });
			callback({ file: blob, nonce: encrypted.nonce });
		};
		r.readAsArrayBuffer(file);
	};

    function getRandomBytes(length) {
        if (!window.crypto && !window.msCrypto && !crypto) {
            throw {
                "errorCode": -1,
                "message": $.t("error_encryption_browser_support")
            };
        }
        var bytes = new Uint8Array(length);
        if (window.crypto) {
            //noinspection JSUnresolvedFunction
            window.crypto.getRandomValues(bytes);
        } else if (window.msCrypto) {
            //noinspection JSUnresolvedFunction
            window.msCrypto.getRandomValues(bytes);
        } else {
            bytes = crypto.randomBytes(length);
        }
        return bytes;
    }

    NRS.encryptMessage = function(NRS, text, senderSecretPhrase, recipientPublicKey, isMessageToSelf) {
        var encrypted = NRS.encryptNote(text, {
            "publicKey": converters.hexStringToByteArray(recipientPublicKey)
        }, senderSecretPhrase);
        if (isMessageToSelf) {
            return {
                encryptToSelfMessageData: encrypted.message,
                encryptToSelfMessageNonce: encrypted.nonce,
                messageToEncryptToSelfIsText: "true"
            }
        } else {
            return {
                encryptedMessageData: encrypted.message,
                encryptedMessageNonce: encrypted.nonce,
                messageToEncryptIsText: "true"
            }
        }
    };

    return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
}