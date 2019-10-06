package com.krikun.mymvvm_arch.arch

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import com.krikun.mymvvm_arch.helpers.coroutines.CoroutineLifecycleAwareScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.krikun.mymvvm_arch.R
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

abstract class BaseDialogFragment : DialogFragment() {

    protected abstract var layout: Int
    protected abstract var isDialogCancelable: Boolean
    abstract var onCancel: () -> Unit
    private var contentView: View? = null
    @get:StyleRes
    abstract val dialogTheme: Int
    //coroutines
    protected val coroutineScope : CoroutineLifecycleAwareScope by inject { parametersOf(this.lifecycle) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        contentView = View.inflate(context, layout, null)
        isCancelable = isDialogCancelable
        if (dialogTheme == R.style.Dialog_Fullscreen) {
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setStyle(STYLE_NO_FRAME, dialogTheme)
        }

        val dialogBuilder = AlertDialog.Builder(requireContext(), dialogTheme)
        contentView?.let { dialogBuilder.setView(it) }

        return dialogBuilder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        contentView

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCancel()
    }

    fun show(manager: FragmentManager) {
        super.show(manager, tag)
    }
}