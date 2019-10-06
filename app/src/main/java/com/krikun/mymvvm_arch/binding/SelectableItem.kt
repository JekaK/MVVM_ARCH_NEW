

package com.krikun.mymvvm_arch.binding

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.krikun.mymvvm_arch.BR

class SelectableItem(defaultItemPosition: Int) : BaseObservable() {

    @Bindable
    var selectedItemPosition: Int = defaultItemPosition
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedItemPosition)
        }
}