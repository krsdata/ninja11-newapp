package ninja.cricks.ui.contest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import ninja.cricks.ContestActivity
import ninja.cricks.R
import ninja.cricks.databinding.DialogFilterBinding


class FilterFragment: BottomSheetDialogFragment() {

    private lateinit var mBinding: DialogFilterBinding
    var filter_text_2 = false
    var filter_text_3 = false
    var filter_text_11 = false
    var filter_text_101 = false
    var filter_text_1001 = false
    var filterGroup2ItemList: ArrayList<Chip> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_filter, container, false
        )
        initValues()
        setViewSelectedItems()
        initClickListeners()
        return mBinding!!.root
    }

    private fun initValues() {
        filter_text_2 = (activity as ContestActivity).filter_text_2
        filter_text_3 = (activity as ContestActivity).filter_text_3
        filter_text_11 = (activity as ContestActivity).filter_text_11
        filter_text_101 = (activity as ContestActivity).filter_text_101
        filter_text_1001 = (activity as ContestActivity).filter_text_1001
        filterGroup2ItemList.clear()
        for(i in (activity as ContestActivity).filterTitleArray.indices) {
            val chip = layoutInflater.inflate(R.layout.item_chip_category,null, false) as Chip
            chip.text = (activity as ContestActivity).filterTitleArray[i].title
            chip.isChecked = (activity as ContestActivity).filterTitleArray[i].selected
            filterGroup2ItemList.add(chip)
            mBinding.chipGroup2.addView(chip)
        }
    }

    private fun initClickListeners() {
        mBinding.imgClose.setOnClickListener{
            this.dismiss()
        }
        mBinding.txtClear.setOnClickListener {
            filter_text_2 = false
            filter_text_3 = false
            filter_text_11 = false
            filter_text_101 = false
            filter_text_1001 = false
            for (i in filterGroup2ItemList) {
                i.isChecked = false
            }
            setViewSelectedItems()
        }

        mBinding.btnApply.setOnClickListener {
             (activity as ContestActivity).filter_text_2 = filter_text_2
             (activity as ContestActivity).filter_text_3 = filter_text_3
             (activity as ContestActivity).filter_text_11 = filter_text_11
             (activity as ContestActivity).filter_text_101 = filter_text_101
             (activity as ContestActivity).filter_text_1001 = filter_text_1001
            (activity as ContestActivity).filter2selected = false
            for(i in (activity as ContestActivity).filterTitleArray) {
                for(j in filterGroup2ItemList) {
                    if (i.title == j.text) {
                        i.selected = j.isChecked
                        if (i.selected) {
                            (activity as ContestActivity).filter2selected = true
                        }
                    }
                }
            }

            val v = (activity as ContestActivity).filter2selected
            val bundle = Bundle()
            parentFragmentManager.setFragmentResult("filter", bundle)
            this.dismiss()
        }

        mBinding.txt2.setOnClickListener {
            filter_text_2 = !filter_text_2
        }
        mBinding.txt3.setOnClickListener {
            filter_text_3 = !filter_text_3
        }
        mBinding.txt11.setOnClickListener {
            filter_text_11 = !filter_text_11
        }
        mBinding.txt101.setOnClickListener {
            filter_text_101 = !filter_text_101
        }
        mBinding.txt1001.setOnClickListener {
            filter_text_1001 = !filter_text_1001
        }
    }

    private fun setViewSelectedItems() {
        mBinding.txt2.isChecked = filter_text_2
        mBinding.txt3.isChecked = filter_text_3
        mBinding.txt11.isChecked = filter_text_11
        mBinding.txt101.isChecked = filter_text_101
        mBinding.txt1001.isChecked = filter_text_1001
    }
}