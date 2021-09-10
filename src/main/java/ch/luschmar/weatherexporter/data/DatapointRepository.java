package ch.luschmar.weatherexporter.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DatapointRepository extends JpaRepository<Datapoint, String>{

}
