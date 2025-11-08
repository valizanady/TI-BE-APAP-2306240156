package apap.ti._5.tour_package_2306240156_be;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
public class TourPackage2306240156BeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourPackage2306240156BeApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedEligibleActivitiesForPkg004(ActivityRepository activityRepository) {
        return args -> {
            Faker faker = new Faker(new Locale("id", "ID"));
            Random random = new Random();

            if (!activityRepository.findAll().isEmpty()) {
                System.out.println("ℹ️ Eligible activities for PKG004 already exist, skipping seeding.");
                return;
            }

            List<Activity> activities = new ArrayList<>();

            // ===== PART 1: 10 Fixed Activities for PKG004 =====
            
            // ✈️ 3 Flight Activities (start ≥ 15:01, end ≤ 15:01 next day)
            String startJakarta = "Kota Administrasi Jakarta Selatan";
            String endJakarta = "Kota Administrasi Jakarta Selatan";
            String[] airlines = {"Garuda Indonesia", "Citilink", "Batik Air", "Lion Air"};

            for (int i = 0; i < 3; i++) {
                Activity flight = new Activity();
                flight.setId(UUID.randomUUID().toString());
                flight.setActivityType("Flight");
                flight.setActivityName("Penerbangan " + faker.options().option(airlines));
                flight.setActivityItem("Tiket " + faker.options().option(airlines) + " kelas ekonomi");
                flight.setCapacity(faker.number().numberBetween(20, 80));
                flight.setPrice((long) faker.number().numberBetween(900_000, 1_800_000));
                flight.setStartLocation(startJakarta);
                flight.setEndLocation(endJakarta);

                // start 15:30–17:00 → valid (>=15:01)
                LocalDateTime start = LocalDateTime.of(2025, 12, 11, faker.number().numberBetween(15, 17), 0);
                flight.setStartDate(start);
                flight.setEndDate(start.plusHours(faker.number().numberBetween(2, 5))); // selesai sebelum 15:01 besok

                activities.add(flight);
            }

            // 🚗 4 Vehicle Rental Activities (start 04:00–10:00, end sebelum 03:52 besok)
            String startAceh = "Kabupaten Aceh Barat";
            String endAceh = "Kabupaten Aceh Barat";
            String[] vehicles = {"Avanza", "Innova", "Fortuner", "Brio", "Xpander"};
            String[] brands = {"Toyota", "Honda", "Mitsubishi", "Suzuki"};

            for (int i = 0; i < 4; i++) {
                Activity rental = new Activity();
                rental.setId(UUID.randomUUID().toString());
                rental.setActivityType("Vehicle Rental");
                rental.setActivityName("Sewa Kendaraan " + faker.options().option(vehicles));
                rental.setActivityItem("Sewa " + faker.options().option(brands) + " " + faker.options().option(vehicles));
                rental.setCapacity(faker.number().numberBetween(5, 20));
                rental.setPrice((long) faker.number().numberBetween(700_000, 1_300_000));
                rental.setStartLocation(startAceh);
                rental.setEndLocation(endAceh);

                // start 04:00–09:00 → valid (>=03:52)
                LocalDateTime start = LocalDateTime.of(2025, 12, 11, faker.number().numberBetween(4, 9), 0);
                rental.setStartDate(start);
                // durasi maksimal 12 jam, pasti selesai < 03:52 besok
                rental.setEndDate(start.plusHours(faker.number().numberBetween(6, 12)));

                activities.add(rental);
            }

            // 🏨 3 Accommodation Activities (start ≥15:03, end ≤15:03 besok)
            String locSimeulue = "KABUPATEN SIMEULUE";
            for (int i = 0; i < 3; i++) {
                Activity hotel = new Activity();
                hotel.setId(UUID.randomUUID().toString());
                hotel.setActivityType("Accommodation");
                hotel.setActivityName("Hotel " + faker.company().name());
                hotel.setActivityItem("Kamar Deluxe di " + faker.address().cityName());
                hotel.setCapacity(faker.number().numberBetween(10, 40));
                hotel.setPrice((long) faker.number().numberBetween(500_000, 800_000));
                hotel.setStartLocation(locSimeulue);
                hotel.setEndLocation(locSimeulue);

                // start 15:30–20:00 → valid
                LocalDateTime start = LocalDateTime.of(2025, 12, 11, faker.number().numberBetween(15, 20), 0);
                hotel.setStartDate(start);
                // durasi 12–20 jam, selesai sebelum 15:03 besok
                hotel.setEndDate(start.plusHours(faker.number().numberBetween(12, 20)));

                activities.add(hotel);
            }

            // ===== PART 2: 10 Random Activities from LocationRestController API =====
            
            try {
                RestTemplate restTemplate = new RestTemplate();
                ObjectMapper mapper = new ObjectMapper();
                
                String provincesUrl = "https://wilayah.id/api/provinces.json";
                String provincesJson = restTemplate.getForObject(provincesUrl, String.class);
                JsonNode provincesNode = mapper.readTree(provincesJson);
                JsonNode provincesData = provincesNode.get("data");
                
                if (provincesData != null && provincesData.isArray() && provincesData.size() > 0) {
                    List<JsonNode> provincesList = new ArrayList<>();
                    provincesData.forEach(provincesList::add);
                    Collections.shuffle(provincesList);
                    
                    int activityCount = 0;
                    String[] typesRandom = {"Flight", "Vehicle Rental", "Accommodation"};
                    
                    for (int p = 0; p < Math.min(2, provincesList.size()) && activityCount < 10; p++) {
                        JsonNode province = provincesList.get(p);
                        String provinceCode = province.get("code").asText();
                        
                        String regenciesUrl = "https://wilayah.id/api/regencies/" + provinceCode + ".json";
                        String regenciesJson = restTemplate.getForObject(regenciesUrl, String.class);
                        JsonNode regenciesNode = mapper.readTree(regenciesJson);
                        JsonNode regenciesData = regenciesNode.get("data");
                        
                        if (regenciesData != null && regenciesData.isArray() && regenciesData.size() > 0) {
                            List<JsonNode> regenciesList = new ArrayList<>();
                            regenciesData.forEach(regenciesList::add);
                            Collections.shuffle(regenciesList);
                            
                            for (int r = 0; r < Math.min(2, regenciesList.size()) && activityCount < 10; r++) {
                                JsonNode regency = regenciesList.get(r);
                                String regencyName = regency.get("name").asText();
                                
                                String type = typesRandom[activityCount % 3];
                                Activity activity = new Activity();
                                activity.setId(UUID.randomUUID().toString());
                                activity.setActivityType(type);
                                activity.setStartLocation(regencyName);
                                activity.setEndLocation(regencyName);
                                
                                if (type.equals("Flight")) {
                                    activity.setActivityName("Penerbangan " + faker.options().option(airlines));
                                    activity.setActivityItem("Tiket " + faker.options().option(airlines) + " kelas ekonomi");
                                    activity.setCapacity(faker.number().numberBetween(30, 70));
                                    activity.setPrice((long) faker.number().numberBetween(1_000_000, 2_000_000));
                                    LocalDateTime start = LocalDateTime.of(2025, 12, 10 + random.nextInt(3), 8 + random.nextInt(8), 0);
                                    activity.setStartDate(start);
                                    activity.setEndDate(start.plusHours(2 + random.nextInt(4)));
                                } else if (type.equals("Vehicle Rental")) {
                                    activity.setActivityName("Sewa Kendaraan " + faker.options().option(vehicles));
                                    activity.setActivityItem("Sewa " + faker.options().option(brands) + " " + faker.options().option(vehicles));
                                    activity.setCapacity(faker.number().numberBetween(5, 18));
                                    activity.setPrice((long) faker.number().numberBetween(600_000, 1_200_000));
                                    LocalDateTime start = LocalDateTime.of(2025, 12, 10 + random.nextInt(3), 6 + random.nextInt(6), 0);
                                    activity.setStartDate(start);
                                    activity.setEndDate(start.plusHours(8 + random.nextInt(6)));
                                } else {
                                    activity.setActivityName("Hotel " + faker.company().name());
                                    activity.setActivityItem("Kamar Deluxe di " + regencyName);
                                    activity.setCapacity(faker.number().numberBetween(10, 35));
                                    activity.setPrice((long) faker.number().numberBetween(450_000, 900_000));
                                    LocalDateTime start = LocalDateTime.of(2025, 12, 10 + random.nextInt(3), 14 + random.nextInt(6), 0);
                                    activity.setStartDate(start);
                                    activity.setEndDate(start.plusHours(12 + random.nextInt(10)));
                                }
                                
                                activities.add(activity);
                                activityCount++;
                            }
                        }
                    }
                    
                    System.out.println("✅ Created " + activityCount + " random activities from API!");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not fetch from API: " + e.getMessage());
            }

            activityRepository.saveAll(activities);
            System.out.println("✅ Total " + activities.size() + " activities seeded (10 fixed + " + (activities.size() - 10) + " random)!");
        };
    }
}

