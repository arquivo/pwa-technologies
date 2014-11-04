var formbuilder3 = {
	errorText: {
		fill: "Por favor, preencha correctamente todos os campos obrigatórios."
	},
	valid: true,
	regras: {
		data: function (valor) {
			var ereg = /^([0-9]{4})-([0-9]{2})-([0-9]{2})$/;
			return ereg.test(valor);
		},
		email: function (valor) {
			var ereg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/
			return ereg.test(valor);
		},
		telefone: function (valor) {
			var ereg = /^([0-9]{9})$/;
			return ereg.test(valor);
		},
		codpostal1: function (valor) {
			var ereg = /^([0-9]{4})$/;
			return ereg.test(valor);
		},
		codpostal2: function (valor) {
			var ereg = /^([0-9]{3})$/;
			return ereg.test(valor);
		}
	},
	fatal: function (obj) {
		formbuilder3.valid = false;
		$(obj).focus();
		$(obj).change(function () {
			$(this).closest(".fb3row").find("label").removeClass("fb3error");
		});
		$(obj).closest(".fb3row").find("label").addClass("fb3error");
		alert(formbuilder3.errorText.fill);
	},
	validate: function (form) {
		console.log(formbuilder3.regras);
	
		formbuilder3.valid = true;
		$(form).find(".fb3obrigatorio").find("input, select, textarea").each(function (i) {
			
			if (!formbuilder3.valid) {
				return;
			} else if (!$(this).is(":checked") && $(this).is(":checkbox")) {
				if ($(this).is(":checkbox") || $(this).is(":radio")) {
					if ($(this).parent().find("input:checked").length > 0) {
						return;
					}
				}				
			} else if ($(this).is(".datepicker")) {
				if (formbuilder3.regras.data($(this).val())) {
					return;
				}
			} else if ($(this).is(".fb3email")) {
				if (formbuilder3.regras.email($(this).val())) {
					return;
				}
			} else if ($(this).is(".fb3telefone")) {
				if (formbuilder3.regras.telefone($(this).val())) {
					return;
				}
			} else if ($(this).is(".fb3codpostal1")) {
				if (formbuilder3.regras.codpostal1($(this).val())) {
					return;
				}
			} else if ($(this).is(".fb3codpostal2")) {
				if (formbuilder3.regras.codpostal2($(this).val())) {
					return;
				}
			} else if ($(this).val()) {
				return;
			}
			
			formbuilder3.fatal(this);
		});
		
		return formbuilder3.valid;
	}
}