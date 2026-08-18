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
     * separes de plus de 24 jours — et immediatement si l'un des deux porte une date en
     * secondes (voir la remarque sur l'unite de creationDate ci-dessous).
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
     * Millisecondes epoch. Attention : deux producteurs ecrivent encore des secondes
     * ({@code DynaConfigLegacyMapper}, {@code HealthLegacyMapper}), ce qui rend toute
     * comparaison entre leurs messages et les autres incoherente. A unifier.
     */
    private long creationDate;

    private Map<String, Object> headers;

    private Object body;

    private List<KeyValue> keyValues;
}
