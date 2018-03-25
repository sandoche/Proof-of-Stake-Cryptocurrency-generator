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
 * @depends {nrs.js}
 */
var NRS = (function(NRS, $) {
	NRS.loadContacts = function() {
		NRS.contacts = {};
		NRS.storageSelect("contacts", null, function (error, contacts) {
			if (contacts && contacts.length) {
				$.each(contacts, function (index, contact) {
					NRS.contacts[contact.account] = contact;
				});
				NRS.logConsole("Loaded " + contacts.length + " contacts");
			}
		});
	};

	NRS.pages.contacts = function() {
		$("#contacts_table_container").show();
		$("#contact_page_database_error").hide();
		if (NRS.isExportContactsAvailable()) {
			$("#export_contacts_button").show();
		} else {
			$("#export_contacts_button").hide();
		}
		NRS.storageSelect("contacts", null, function (error, contacts) {
			var rows = "";
			if (contacts && contacts.length) {
				contacts.sort(function (a, b) {
					if (a.name.toLowerCase() > b.name.toLowerCase()) {
						return 1;
					} else if (a.name.toLowerCase() < b.name.toLowerCase()) {
						return -1;
					} else {
						return 0;
					}
				});
				$.each(contacts, function (index, contact) {
					var contactDescription = contact.description;
					if (contactDescription.length > 100) {
						contactDescription = contactDescription.substring(0, 100) + "...";
					} else if (!contactDescription) {
						contactDescription = "-";
					}
					rows += "<tr>" +
						"<td><a href='#' data-toggle='modal' data-target='#update_contact_modal' data-contact='" + String(contact.id).escapeHTML() + "'>" + contact.name.escapeHTML() + "</a></td>" +
						"<td><a href='#' data-user='" + NRS.getAccountFormatted(contact, "account") + "' class='show_account_modal_action user_info'>" + NRS.getAccountFormatted(contact, "account") + "</a></td>" +
						"<td>" + (contact.email ? contact.email.escapeHTML() : "-") + "</td>" +
						"<td>" + contactDescription.escapeHTML() + "</td>" +
						"<td style='white-space:nowrap'>" +
						"<a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#send_money_modal' data-contact='" + String(contact.name).escapeHTML() + "'>" + $.t("send_nxt") + "</a>&nbsp;" +
						"<a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#send_message_modal' data-contact='" + String(contact.name).escapeHTML() + "'>" + $.t("message") + "</a>&nbsp;" +
						"<a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#delete_contact_modal' data-contact='" + String(contact.id).escapeHTML() + "'>" + $.t("delete") + "</a>" +
						"</td>" +
					"</tr>";
				});
			}
			NRS.dataLoaded(rows);
		});
	};

	NRS.forms.addContact = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		data.account_id = String(data.account_id);
		if (!data.name) {
			return {
				"error": $.t("error_contact_name_required")
			};
		} else if (!data.account_id) {
			return {
				"error": $.t("error_account_id_required")
			};
		}

		if (/^\d+$/.test(data.name) || /^NXT\-/i.test(data.name)) {
			return {
				"error": $.t("error_contact_name_alpha")
			};
		}

		if (data.email && !/@/.test(data.email)) {
			return {
				"error": $.t("error_email_address")
			};
		}

		if (data.account_id.charAt(0) == '@') {
			var convertedAccountId = $modal.find("input[name=converted_account_id]").val();
			if (convertedAccountId) {
				data.account_id = convertedAccountId;
			} else {
				return {
					"error": $.t("error_account_id")
				};
			}
		}
		var address;
		if (/^NXT\-/i.test(data.account_id)) {
			data.account_rs = data.account_id;
			address = new NxtAddress();
			if (address.set(data.account_rs)) {
				data.account = address.account_id();
			} else {
				return {
					"error": $.t("error_account_id")
				};
			}
		} else {
			address = new NxtAddress();
			if (address.set(data.account_id)) {
				data.account_rs = address.toString();
			} else {
				return {
					"error": $.t("error_account_id")
				};
			}
		}

		NRS.sendRequest("getAccount", {
			"account": data.account_id
		}, function(response) {
			if (!response.errorCode) {
				if (response.account != data.account || response.accountRS != data.account_rs) {
					return {
						"error": $.t("error_account_id")
					};
				}
			}
		}, { isAsync: false });

		var $btn = $modal.find("button.btn-primary:not([data-dismiss=modal], .ignore)");

		NRS.storageSelect("contacts", [{
			"account": data.account_id
		}, {
			"name": data.name
		}], function (error, contacts) {
			if (contacts && contacts.length) {
				if (contacts[0].name == data.name) {
					$modal.find(".error_message").html($.t("error_contact_name_exists")).show();
				} else {
					$modal.find(".error_message").html($.t("error_contact_account_id_exists")).show();
				}
				$btn.button("reset");
				$modal.modal("unlock");
			} else {
				NRS.storageInsert("contacts", "name", {
					name: data.name,
					email: data.email,
					account: data.account_id,
					accountRS: data.account_rs,
					description: data.description
				}, function () {
					NRS.contacts[data.account_id] = {
						name: data.name,
						email: data.email,
						account: data.account_id,
						accountRS: data.account_rs,
						description: data.description
					};
					setTimeout(function () {
						$btn.button("reset");
						$modal.modal("unlock");
						$modal.modal("hide");
						$.growl($.t("success_contact_add"), {
							"type": "success"
						});
						if (NRS.currentPage == "contacts") {
							NRS.loadPage("contacts");
						} else if (NRS.currentPage == "messages" && NRS.selectedContext) {
							var heading = NRS.selectedContext.find("h4.list-group-item-heading");
							if (heading.length) {
								heading.html(data.name.escapeHTML());
							}
							NRS.selectedContext.data("context", "messages_sidebar_update_context");
						}
					}, 50);
				}, true);
			}
		});
	};

	$("#update_contact_modal").on("show.bs.modal", function(e) {
		var $invoker = $(e.relatedTarget);
		var contactId = parseInt($invoker.data("contact"), 10);
		if (!contactId && NRS.selectedContext) {
			var accountId = NRS.selectedContext.data("account");
			var dbKey = (/^NXT\-/i.test(accountId) ? "accountRS" : "account");
			var dbQuery = {};
			dbQuery[dbKey] = accountId;
			NRS.storageSelect("contacts", [dbQuery], function(error, contact) {
				contact = contact[0];
				$("#update_contact_id").val(contact.id);
				$("#update_contact_name").val(contact.name);
				$("#update_contact_email").val(contact.email);
				$("#update_contact_account_id").val(contact.accountRS);
				$("#update_contact_description").val(contact.description);
			});
		} else {
			$("#update_contact_id").val(contactId);
			NRS.storageSelect("contacts", [{
				"id": contactId
			}], function(error, contact) {
				contact = contact[0];
				$("#update_contact_name").val(contact.name);
				$("#update_contact_email").val(contact.email);
				$("#update_contact_account_id").val(contact.accountRS);
				$("#update_contact_description").val(contact.description);
			});
		}
	});

	NRS.forms.updateContact = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		data.account_id = String(data.account_id);
		if (!data.name) {
			return {
				"error": $.t("error_contact_name_required")
			};
		} else if (!data.account_id) {
			return {
				"error": $.t("error_account_id_required")
			};
		}

		if (data.account_id.charAt(0) == '@') {
			var convertedAccountId = $modal.find("input[name=converted_account_id]").val();
			if (convertedAccountId) {
				data.account_id = convertedAccountId;
			} else {
				return {
					"error": $.t("error_account_id")
				};
			}
		}
		var contactId = parseInt($("#update_contact_id").val(), 10);
		if (!contactId) {
			return {
				"error": $.t("error_contact")
			};
		}
		var address;
		if (/^NXT\-/i.test(data.account_id)) {
			data.account_rs = data.account_id;
			address = new NxtAddress();
			if (address.set(data.account_rs)) {
				data.account = address.account_id();
			} else {
				return {
					"error": $.t("error_account_id")
				};
			}
		} else {
			address = new NxtAddress();
			if (address.set(data.account_id)) {
				data.account_rs = address.toString();
			} else {
				return {
					"error": $.t("error_account_id")
				};
			}
		}

		NRS.sendRequest("getAccount", {
			"account": data.account_id
		}, function(response) {
			if (!response.errorCode) {
				if (response.account != data.account_id || response.accountRS != data.account_rs) {
					return {
						"error": $.t("error_account_id")
					};
				}
			}
		}, { isAsync: false });

		var $btn = $modal.find("button.btn-primary:not([data-dismiss=modal])");
		NRS.storageSelect("contacts", [{
			"account": data.account_id
		}], function(error, contacts) {
			if (contacts && contacts.length && contacts[0].id != contactId) {
				$modal.find(".error_message").html($.t("error_contact_exists")).show();
				$btn.button("reset");
				$modal.modal("unlock");
			} else {
				NRS.storageUpdate("contacts", {
					name: data.name,
					email: data.email,
					account: data.account_id,
					accountRS: data.account_rs,
					description: data.description
				}, [{
					"id": contactId
				}], function() {
					if (contacts.length && data.account_id != contacts[0].accountId) {
						delete NRS.contacts[contacts[0].accountId];
					}
					NRS.contacts[data.account_id] = {
						name: data.name,
						email: data.email,
						account: data.account_id,
						accountRS: data.account_rs,
						description: data.description
					};

					setTimeout(function() {
						$btn.button("reset");
						$modal.modal("unlock");
						$modal.modal("hide");
						$.growl($.t("success_contact_update"), {
							"type": "success"
						});

						if (NRS.currentPage == "contacts") {
							NRS.loadPage("contacts");
						} else if (NRS.currentPage == "messages" && NRS.selectedContext) {
							var heading = NRS.selectedContext.find("h4.list-group-item-heading");
							if (heading.length) {
								heading.html(data.name.escapeHTML());
							}
						}
					}, 50);
				});
			}
		});
	};

	$("#delete_contact_modal").on("show.bs.modal", function(e) {
		var $invoker = $(e.relatedTarget);
		var contactId = $invoker.data("contact");
		$("#delete_contact_id").val(contactId);
		NRS.storageSelect("contacts", [{
			"id": contactId
		}], function(error, contact) {
			contact = contact[0];
			$("#delete_contact_name").html(contact.name.escapeHTML());
			$("#delete_contact_account_id").val(NRS.getAccountFormatted(contact, "account"));
		});
	});

	NRS.forms.deleteContact = function() {
		var id = parseInt($("#delete_contact_id").val(), 10);
		NRS.storageDelete("contacts", [{
			"id": id
		}], function() {
			delete NRS.contacts[$("#delete_contact_account_id").val()];
			setTimeout(function() {
				$.growl($.t("success_contact_delete"), {
					"type": "success"
				});

				if (NRS.currentPage == "contacts") {
					NRS.loadPage("contacts");
				}
			}, 50);
		});
		return {
			"stop": true
		};
	};

	NRS.exportContacts = function() {
		if (NRS.contacts && (Object.keys(NRS.contacts).length > 0)) {
			var contacts_download = document.createElement('a');
			contacts_download.href = 'data:text/json,' + JSON.stringify( NRS.contacts );
			contacts_download.target = '_blank';
			contacts_download.download = 'contacts.json';
			document.body.appendChild(contacts_download);
			contacts_download.click();
			document.body.removeChild(contacts_download);
		} else {
			$.growl($.t("error_no_contacts_available"), {"type":"warning"}).show();
		}
	};
	$("#export_contacts_button").on("click", function() {
		NRS.exportContacts();
	});

	NRS.importContacts = function(imported_contacts) {
		$.each(imported_contacts, function(index, imported_contact) {
			NRS.storageSelect("contacts", [{
				"account": imported_contact.account
			}, {
				"name": imported_contact.name
			}], function(error, contacts) {
				if (contacts && contacts.length) {
					if (contacts[0].name == imported_contact.name) {
						$.growl(imported_contact.name + ' - ' + $.t("error_contact_name_exists"), {"type":"warning"}).show();
					} else {
						$.growl(imported_contact.account + ' - ' + $.t("error_contact_account_id_exists"), {"type":"warning"}).show();
					}
				} else {
					NRS.storageInsert("contacts", "name", {
						name: imported_contact.name,
						email: imported_contact.email,
						account: imported_contact.account,
						accountRS: imported_contact.accountRS,
						description: imported_contact.description
					}, function() {
						NRS.contacts[imported_contact.account] = {
							name: imported_contact.name,
							email: imported_contact.email,
							account: imported_contact.account,
							accountRS: imported_contact.accountRS,
							description: imported_contact.description
						};

						setTimeout(function() {
							$.growl(imported_contact.name + ' - ' + $.t("success_contact_add"), {
								"type": "success"
							});
							if (NRS.currentPage == "contacts") {
								NRS.loadPage("contacts");
							} else if (NRS.currentPage == "messages" && NRS.selectedContext) {
								var heading = NRS.selectedContext.find("h4.list-group-item-heading");
								if (heading.length) {
									heading.html(imported_contact.name.escapeHTML());
								}
								NRS.selectedContext.data("context", "messages_sidebar_update_context");
							}
						}, 50);
					}, true);
				}
			});
		});
	};

	var importContactsButtonField = $("#import_contacts_button_field");
    importContactsButtonField.css({'display':'none'});
	importContactsButtonField.on("change", function(button_event) {
		button_event.preventDefault();
            var importContactsButtonField = $("#import_contacts_button_field");
            var file = importContactsButtonField[0].files[0];
            var reader = new FileReader();
            reader.onload = function (read_event) {
                var imported_contacts = JSON.parse(read_event.target.result);
                NRS.importContacts(imported_contacts);
            };
            reader.readAsText(file);
            var button = importContactsButtonField;
            button.replaceWith(button.clone(true) ); // Recreate button to clean it
		return false;
	});

	$("#import_contacts_button").on("click", function() {
		if (NRS.isFileReaderSupported()) {
            $("#import_contacts_button_field").click();
        } else if (window.java) {
            var result = java.readContactsFile();
            var contacts = JSON.parse(result);
            if (contacts.error) {
                if (contacts.type == 1) {
                    $.growl($.t(contacts.error, { file: contacts.file, folder: contacts.folder }));
                } else {
                    $.growl(contacts.error);
                }
            } else {
                NRS.importContacts(contacts);
            }
        }
	});

	return NRS;
}(NRS || {}, jQuery));