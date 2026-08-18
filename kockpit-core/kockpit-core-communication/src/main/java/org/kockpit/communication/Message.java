package org.kockpit.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Message {

    /**
     * Ordre chronologique croissant, du plus ancien au plus recent.
     *
     * <p>A utiliser systematiquement plutot qu'un comparateur par soustraction : creationDate
     * est un long en millisecondes epoch, donc {@code (int)(a - b)} ou
     * {@code Math.toIntExact(a - b)} depasse la capacite d'un int des que deux messages sont
     * separes de plus de 24 jours.
     */
    public static final Comparator<Message> BY_CREATION_DATE =
            Comparator.comparingLong(Message::getCreationDate);

    private String id;

    private String service;

    private String type;

    private String domain;

    private String env;

    private String appId;

    /**
     * Millisecondes epoch, pour tous les producteurs sans exception. L'unite fait partie du
     * contrat : {@code HeartBeatManager} compare cette valeur a un TTL exprime en
     * millisecondes, et {@link #BY_CREATION_DATE} n'ordonne correctement que des valeurs de
     * meme unite. Une date en secondes serait interpretee comme datant de 1970.
     */
    private long creationDate;

    private Map<String, Object> headers;

    private Object body;

    private List<KeyValue> keyValues;
}
