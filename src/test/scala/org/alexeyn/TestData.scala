package org.alexeyn

import java.time.LocalDate

object TestData {
  val adId = 1
  val toyotaAd = CarAd(adId, "toyota", Fuel.Diesel, 20000, `new` = true, None, Some(LocalDate.of(2010, 4, 22)))

  val mockData: IndexedSeq[CarAd] =
    IndexedSeq(
      CarAd(2, "honda", Fuel.Gasoline, 2000, `new` = false, Some(20000), Some(LocalDate.of(2000, 4, 22))),
      toyotaAd,
      CarAd(3, "ford", Fuel.Gasoline, 2000, `new` = true, Some(10000), Some(LocalDate.of(2010, 5, 12)))
    )
}
