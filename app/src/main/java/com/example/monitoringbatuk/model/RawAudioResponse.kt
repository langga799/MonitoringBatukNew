package com.example.monitoringbatuk.modelimport com.google.gson.annotations.SerializedNamedata class RawAudioResponse(	@field:SerializedName("protected")	val jsonMemberProtected: JsonMemberProtected,	@field:SerializedName("signature")	val signature: String,	@field:SerializedName("payload")	val payload: PayloadData)data class SensorsDev(	@field:SerializedName("name")	val name: String,	@field:SerializedName("units")	val units: String)data class PayloadData(	@field:SerializedName("interval_ms")	val intervalMs: Double,	@field:SerializedName("sensors")	val sensors: List<SensorsDev>,	@field:SerializedName("values")	val values: List<Int>,	@field:SerializedName("device_type")	val deviceType: String)data class JsonMemberProtected(	@field:SerializedName("ver")	val ver: String,	@field:SerializedName("alg")	val alg: String)