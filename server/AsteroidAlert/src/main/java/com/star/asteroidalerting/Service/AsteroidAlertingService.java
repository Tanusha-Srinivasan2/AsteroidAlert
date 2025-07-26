package com.star.asteroidalerting.Service;


import com.star.asteroidalerting.client.NasaClient;
import com.star.asteroidalerting.dto.Asteroid;
import com.star.asteroidalerting.event.AsteroidCollisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AsteroidAlertingService {

    private final NasaClient nasaClient;
    private final KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate;

    @Autowired
    public AsteroidAlertingService(NasaClient nasaClient, KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate) {
        this.nasaClient = nasaClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    public void alert() {
        log.info("Alerting service called");
        //from and to date
        final LocalDate fromDate = LocalDate.of(2017,7, 15);
        final LocalDate toDate = LocalDate.of(2017,7,15);
        //call nasa api to get asteroid data
        log.info("Getting asteroid list for dates:{} to {}", fromDate, toDate);
        final List<Asteroid> asteroidList = nasaClient.getNeoAsteroids(fromDate, toDate);
        log.info("Received Asteroid List of size: {}", asteroidList.size());
        //if any hazardous, send alert
        final List<Asteroid> dangerousAsteroids = asteroidList.stream()
                .filter(Asteroid::isPotentiallyHazardous)
                .toList();
        log.info("Found Hazardous Asteroids: {}", dangerousAsteroids.size());
        // create an alert and put on kafka topic
        final List<AsteroidCollisionEvent> asteroidCollisionEventList = createEventListOfDangerousAsteroids(dangerousAsteroids);
        log.info("Sending {} asteroid alerts to Kafka", asteroidCollisionEventList.size());
        asteroidCollisionEventList.forEach(event -> {
            kafkaTemplate.send("asteroid-alert", event);
            log.info("Asteroid alert sent to Kafka topic:{}", event);
        });



    }
    private List<AsteroidCollisionEvent> createEventListOfDangerousAsteroids(final List<Asteroid> dangerousAsteroids){
        return dangerousAsteroids.stream()
                .map(asteroid -> {
                    if (asteroid.isPotentiallyHazardous()) {
                        return AsteroidCollisionEvent.builder()
                                .asteroidName(asteroid.getName())
                                .closeApproachDate(
                                        asteroid.getCloseApproachData().isEmpty() || asteroid.getCloseApproachData().getFirst().getCloseApproachDate() == null
                                                ? "UNKNOWN"
                                                : asteroid.getCloseApproachData().getFirst().getCloseApproachDate().toString()
                                )

                                .missDistanceKilometers(asteroid.getCloseApproachData().getFirst().getMissDistance().getKilometers())
                                .estimatedDiameterAvgMeters((asteroid.getEstimatedDiameter().getMeters().getMinDiameters() + asteroid.getEstimatedDiameter().getMeters().getMaxDiameters()) / 2)
                                .build();
                    }
                    return null;
                })
                .toList();

    }
}
