package com.example.filamenthmi.render

import android.widget.TextView
import com.example.filamenthmi.R

class HudBinder(
    private val gear: TextView,
    private val warning: TextView,
    private val speed: TextView,
    private val unit: TextView,
    private val acc: TextView,
    private val speedLimit: TextView
) {
    fun bind(state: HudUiState) {
        gear.text = state.gear
        warning.alpha = if (state.warningRingRed) 1f else 0.25f
        speed.text = HudFormatter.speed(state.speedKmh)
        unit.text = HudFormatter.unit()
        acc.alpha = if (state.accEnabled) 1f else 0.35f
        speedLimit.text = state.speedLimitKmh.toString()
    }

    companion object {
        fun fromActivity(activity: androidx.appcompat.app.AppCompatActivity): HudBinder = HudBinder(
            gear = activity.findViewById(R.id.hudGear),
            warning = activity.findViewById(R.id.hudWarning),
            speed = activity.findViewById(R.id.hudSpeed),
            unit = activity.findViewById(R.id.hudUnit),
            acc = activity.findViewById(R.id.hudAcc),
            speedLimit = activity.findViewById(R.id.hudSpeedLimit)
        )
    }
}
