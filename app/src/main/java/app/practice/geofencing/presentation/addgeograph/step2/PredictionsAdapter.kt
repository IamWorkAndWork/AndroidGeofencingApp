package app.practice.geofencing.presentation.addgeograph.step2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.practice.geofencing.databinding.PredictionsRowLayoutBinding
import app.practice.geofencing.util.MyDiffUtil
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PredictionsAdapter : RecyclerView.Adapter<PredictionsAdapter.MyViewHolder>() {

    private var predictions = emptyList<AutocompletePrediction>()

    private val _placeId = MutableStateFlow("")
    val placeId: StateFlow<String> get() = _placeId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent, _placeId)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentPrediction = predictions[position]
        holder.bind(currentPrediction)
    }

    override fun getItemCount(): Int {
        return predictions.size
    }

    fun setData(newPredictions: List<AutocompletePrediction>) {
        val myDiffUtil = MyDiffUtil(predictions, newPredictions)
        val myDiffUtilResult = DiffUtil.calculateDiff(myDiffUtil)
        predictions = newPredictions
        myDiffUtilResult.dispatchUpdatesTo(this)
    }

    class MyViewHolder(
        private val binding: PredictionsRowLayoutBinding,
        private var placeId: MutableStateFlow<String>
    ) :
        RecyclerView.ViewHolder(binding.root) {

        var prediction: AutocompletePrediction? = null

        companion object {
            fun from(parent: ViewGroup, placeId: MutableStateFlow<String>): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PredictionsRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding, placeId)
            }
        }

        init {
            binding.rootLayout.setOnClickListener {
                prediction?.let { _prediction ->
                    setPlaceId(_prediction.placeId)
                }
            }
        }

        fun bind(prediction: AutocompletePrediction) {
            this@MyViewHolder.prediction = prediction
            binding.prediction = prediction
        }

        private fun setPlaceId(placeId: String) {
            this@MyViewHolder.placeId.value = placeId
        }

    }

}