package com.accor.wcp.sample.api.openapi;

import com.accor.wcp.sample.basket.api.BasketsApiDelegate;
import com.accor.wcp.sample.basket.api.model.Basket;
import com.accor.wcp.sample.basket.api.model.GenderEnum;
import com.accor.wcp.sample.basket.api.model.HotelStayBasketItem;
import com.accor.wcp.sample.basket.api.model.HotelStayGuest;
import com.accor.wcp.sample.basket.api.model.HotelStayMainBeneficiary;
import com.accor.wcp.sample.basket.api.model.OfferTypeEnum;
import com.accor.wcp.sample.basket.api.model.PhoneNumber;
import com.accor.wcp.sample.basket.api.model.StandaloneTreatmentBasketItem;
import com.accor.wcp.sample.basket.api.model.StandaloneTreatmentGuest;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class BasketApiImpl implements BasketsApiDelegate {

  @Override
  public ResponseEntity<Basket> getBasketById(
      String xAcceptVersion,
      String basketId,
      String xWcpTraceId,
      String xWcpOrigin,
      String acceptLanguage,
      String fields) {
    Basket basket = new Basket();
    basket.setId(basketId);

    PhoneNumber phone = new PhoneNumber();
    phone.setNumber("602733919");
    phone.setPrefix("33");

    HotelStayBasketItem hotelStayBasketItem = new HotelStayBasketItem();
    hotelStayBasketItem.setId(UUID.randomUUID().toString());
    hotelStayBasketItem.setOfferId(UUID.randomUUID().toString());
    hotelStayBasketItem.setOfferType(OfferTypeEnum.HOTEL_STAY);
    HotelStayGuest guestsItemTom = new HotelStayGuest();
    guestsItemTom.setAge(11);
    guestsItemTom.setFirstName("Tom");
    guestsItemTom.setLastName("Joui");
    HotelStayGuest guestsItemZoe = new HotelStayGuest();
    guestsItemZoe.setAge(8);
    guestsItemZoe.setFirstName("Zoé");
    guestsItemZoe.setLastName("Joui");

    hotelStayBasketItem.addGuestsItem(guestsItemTom);
    hotelStayBasketItem.addGuestsItem(guestsItemZoe);
    hotelStayBasketItem.setComment("Nous arriverons tard en soirée.");
    HotelStayMainBeneficiary mainBeneficiary = new HotelStayMainBeneficiary();
    mainBeneficiary.email("cyril@kiss.fr");
    mainBeneficiary.setFirstName("Cyril");
    mainBeneficiary.setLastName("Joui");
    mainBeneficiary.setPhone(phone);
    hotelStayBasketItem.setMainBeneficiary(mainBeneficiary);

    StandaloneTreatmentBasketItem standaloneTreatmentBasketItem =
        new StandaloneTreatmentBasketItem();
    standaloneTreatmentBasketItem.setId(UUID.randomUUID().toString());
    standaloneTreatmentBasketItem.setOfferId(UUID.randomUUID().toString());
    standaloneTreatmentBasketItem.setOfferType(OfferTypeEnum.STANDALONE_TREATMENT);
    StandaloneTreatmentGuest standaloneTreatmentGuest = new StandaloneTreatmentGuest();
    standaloneTreatmentGuest.email("cyril@kiss.fr");
    standaloneTreatmentGuest.setFirstName("Cyril");
    standaloneTreatmentGuest.setLastName("Joui");
    standaloneTreatmentGuest.gender(GenderEnum.MALE);
    standaloneTreatmentBasketItem.setGuest(standaloneTreatmentGuest);
    standaloneTreatmentGuest.setPhone(phone);

    basket.setItems(List.of(hotelStayBasketItem, standaloneTreatmentBasketItem));
    return ResponseEntity.ok(basket);
  }

  @Override
  public ResponseEntity<Basket> createBasket(
      String xWcpTraceId, String xWcpOrigin, String acceptLanguage, String fields, Basket basket) {
    return getBasketById(
        acceptLanguage,
        UUID.randomUUID().toString(),
        xWcpTraceId,
        xWcpOrigin,
        acceptLanguage,
        fields);
  }
}
