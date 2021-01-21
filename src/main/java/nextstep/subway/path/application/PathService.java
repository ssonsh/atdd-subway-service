package nextstep.subway.path.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Lines;
import nextstep.subway.line.domain.Section;
import nextstep.subway.path.dto.PathFindResponse;
import nextstep.subway.path.dto.PathRequest;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PathService {
    private final LineRepository lineRepository;
    private final StationService stationService;

    public PathService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    @Transactional(readOnly = true)
    public PathResponse findShortestPath(PathRequest pathRequest) {
        Lines lines = new Lines(lineRepository.findAll());
        Station source = stationService.getOne(pathRequest.getSourceId());
        Station target = stationService.getOne(pathRequest.getTargetId());
        if (source == target) {
            throw new RuntimeException();
        }
        PathFindResponse pathFindResponse = PathFinder.findPath(lines.getLines(), source, target);
        int distance = lines.getDistance(pathFindResponse.getStationIds());
        List<StationResponse> stations = lines.getStations(pathFindResponse.getStationIds())
                .stream()
                .map(StationResponse::new)
                .collect(Collectors.toList());
        return new PathResponse(distance, stations);
    }


}
