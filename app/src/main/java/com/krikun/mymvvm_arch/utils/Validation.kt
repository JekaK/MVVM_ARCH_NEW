

package com.krikun.mymvvm_arch.utils

import android.text.InputFilter
import android.view.View
import android.widget.EditText
import androidx.core.util.PatternsCompat
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.widget.RxTextView
import com.krikun.mymvvm_arch.R
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.trello.rxlifecycle3.kotlin.bindToLifecycle

sealed class ValidationType {
    object Text : ValidationType()
    object Email : ValidationType()
    data class Password(val minLength: Int, val maxLength: Int, val regex: Regex = ".*".toRegex()) : ValidationType()
    /** Use this type only with com.redmadrobot:inputmask lib. **/
    data class Phone(val staticPartRegex: Regex, val phoneRegex: Regex, val phoneMask: String) : ValidationType()
}

// Length
const val VALIDATABLE_MAX_LENGTH = 1000

/**
 * Validates input fields. Better use with delegate. Sample below.
 *
 * ---In your fragment---
 *
 * private val validationDelegate: ProfileEditValidation = ProfileEditValidation(this)
 *
 * ...
 *
 * onViewCreated() {
 *   // Use validation
 *   val isValid = validationDelegate.validate()
 * }
 *
 * ---Delegate class sample---
 *
 * class ProfileEditValidation(val fragment: MyProfileEditFragment) : FragmentViewLifecycleObserver(fragment) {
 *
 *   lateinit var validator: Validator
 *
 *   fun initValidator() {
 *       fragment.run {
 *           validator = Validator.create(
 *               Validator.ValidatableItem(0, etProfileName, tilProfileName, min = 2, max = 20),
 *               Validator.ValidatableItem(1, etProfileSurname, tilProfileSurname, min = 2, max = 20),
 *               Validator.ValidatableItem(2, etProfileEmail, tilProfileEmail, ValidationType.Email))
 *       }
 *   }
 *
 *   fun validate(): Boolean = validator.validateAll()
 *
 *   override fun afterViewCreated() = initValidator()
 *  }
 *
 * */


class Validator {
    private val items: MutableList<ValidatableItem> = mutableListOf()

    companion object {
        fun create(vararg validatableItem: ValidatableItem): Validator {
            return Validator().apply { init(validatableItem) }
        }
    }

    fun init(validationItems: Array<out ValidatableItem>) {
        items.addAll(validationItems)
        // Remove errors on new char entered
        for (item in items) {
            handleValidatableFields(this to item)
        }
    }

    fun getValidatableItemByKey(key: Int) = items.firstOrNull { it.key == key }

    fun validateItem(key: Int, showErrorUi: Boolean = true, optional: Boolean = false): Boolean {
        return items.firstOrNull { it.key == key }?.let {
            validateField(
                    it.copy(
                            showErrorUi = showErrorUi,
                            optional = { optional }
                    )
            )
        } ?: false
    }

    fun validateItem(validatableItem: ValidatableItem): Boolean {
        return items.firstOrNull { it.key == validatableItem.key }?.let { validateField(validatableItem) } ?: false
    }

    fun validateAll(): Boolean {
        var isValid = true
        for (item in items) {
            validateField(item).apply {
                //if at least one item false then all validation false
                if (!this && isValid) {
                    isValid = false
                }
            }
        }

        return isValid
    }

    data class ValidatableItem(
        val key: Int,
        val editText: EditText,
        val textInputLayout: TextInputLayout,
        val validationType: ValidationType = ValidationType.Text,
        val min: Int = if (validationType is ValidationType.Password) validationType.minLength else 1,
        val max: Int = if (validationType is ValidationType.Password) validationType.maxLength else VALIDATABLE_MAX_LENGTH,
        val onError: String = getString(R.string.error_invalid_data),
        val onEmpty: String = getString(R.string.error_empty_field),
        val onLessThenMin: String = getString(R.string.error_minimum_length).format(min),
        val onMoreThenMax: String = getString(R.string.error_maximum_length).format(max),
        val additionalPredicate: () -> Boolean = { true },
        val showErrorUi: Boolean = true,
        val optional: () -> Boolean = { false },
        val validateOnFocusChange: Boolean = true,
        val additionalFocusListener: View.OnFocusChangeListener? = null,
        val showErrorContainerAlways: Boolean = false
    )
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_UTILS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun EditText.asPhoneField(validator: Validator?, phoneValidatableItemKey: Int = -1) {
    val validatableItem = validator?.getValidatableItemByKey(phoneValidatableItemKey)
    if (validatableItem?.validationType !is ValidationType.Phone) return
    val config = validatableItem.validationType as ValidationType.Phone
    postSelf {
        val listener = MaskedTextChangedListener(config.phoneMask, this,
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        // Do nothing
                    }
                })

        this.addTextChangedListener(listener)
        this.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            listener.onFocusChange(v, hasFocus)
            if (!hasFocus) {
                if (this.value().matches(config.staticPartRegex)) {
                    this.clearText()
                } else {
                    errorSafety { validator.validateItem(validatableItem) }
                }
            }
        }
    }
}


fun validateField(validatableItem: Validator.ValidatableItem): Boolean {
    with(validatableItem) {
        editText.value().let { value ->
            return when {
                value.isBlank() && validatableItem.optional() -> true
                value.isBlank() -> if (showErrorUi) textInputLayout.handleError(
                        onEmpty,
                        showErrorContainerAlways = validatableItem.showErrorContainerAlways
                ).let { false } else false
                value.length < min -> if (showErrorUi) textInputLayout.handleError(
                        onLessThenMin,
                        showErrorContainerAlways = validatableItem.showErrorContainerAlways
                ).let { false } else false
                value.length > max -> if (showErrorUi) textInputLayout.handleError(
                        onMoreThenMax,
                        showErrorContainerAlways = validatableItem.showErrorContainerAlways
                ).let { false } else false
                else -> {
                    return (additionalPredicate() and when (validationType) {
                        ValidationType.Text -> true
                        ValidationType.Email -> validateEmail(value)
                        is ValidationType.Password -> value.matches(validationType.regex)
                        is ValidationType.Phone -> if (value.matches(validationType.staticPartRegex)) {
                            if (validatableItem.optional()) {
                                editText.clearText()
                                true
                            } else {
                                false
                            }
                        } else {
                            value.matches(validationType.phoneRegex)
                        }
                    }).apply {
                        if (!this && showErrorUi) {
                            textInputLayout.handleError(
                                    onError,
                                    showErrorContainerAlways = validatableItem.showErrorContainerAlways
                            )
                        }
                    }
                }
            }.also { valid ->
                // If valid - clear error
                if (valid) {
                    if (validatableItem.showErrorContainerAlways) {
                        textInputLayout.error = null
                    } else {
                        textInputLayout.isErrorEnabled = false
                    }
                }
            }
        }
    }
}

fun handleValidatableFields(vararg validationItem: Pair<Validator, Validator.ValidatableItem>) {
    for ((validator, validatableItem) in validationItem) {
        val editText = validatableItem.editText
        val textInputLayout = validatableItem.textInputLayout

        // Validate on focus change. Phone field focus change validation do manually
        if (validatableItem.validationType is ValidationType.Phone) {
            editText.asPhoneField(validator, validatableItem.key)
        } else {
            editText.setOnFocusChangeListener { v, hasFocus ->
                validatableItem.additionalFocusListener?.onFocusChange(v, hasFocus)

                if (hasFocus.inverse() && validatableItem.validateOnFocusChange) {
                    validateField(validatableItem)
                }
            }
        }
        // Handle max length
        editText.filters = editText.filters
                .toMutableList()
                .apply { add(InputFilter.LengthFilter(validatableItem.max)) }
                .toTypedArray()
        // Error container initial visibility
        textInputLayout.isErrorEnabled = validatableItem.showErrorContainerAlways
        // Clearing error on new char entered
        editText.post {
            RxTextView.textChanges(editText)
                    .skipInitialValue()
                    .filter { textInputLayout.isErrorEnabled }
                    .bindToLifecycle(editText)
                    .subscribe({
                        if (validatableItem.showErrorContainerAlways) {
                            textInputLayout.error = null
                        } else {
                            textInputLayout.isErrorEnabled = false
                        }
                    }, { it.print() })
        }
    }
}

fun TextInputLayout.handleError(message: String?, animate: Boolean = false, showErrorContainerAlways: Boolean = false) {
    fun action() {
        if (message != null) {
            error = message
        } else {
            if (showErrorContainerAlways) {
                error = null
            } else {
                isErrorEnabled = false
            }
        }
    }
    if (animate) {
        animate {
            action()
        }
    } else {
        action()
    }
}

fun validateEmail(email: String) =
        email.matches(PatternsCompat.EMAIL_ADDRESS.toRegex()) && email.first() != '.' && !email.contains(".@")