class RealtimeResponse(val status: String, val result: Result) {

    class Result(val realtime: Realtime)

    class Realtime(val skycon: String, val temperature: Float)

    class AirQuality(val aqi: AQI)

    class AQI(val chn: Float)

}