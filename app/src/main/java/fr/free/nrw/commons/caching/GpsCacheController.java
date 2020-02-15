package fr.free.nrw.commons.caching;

import com.github.varunpant.quadtree.Point;
import com.github.varunpant.quadtree.QuadTree;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.free.nrw.commons.mwapi.CategoryApi;
import fr.free.nrw.commons.upload.metadata.GPSCoordinates;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Singleton
public class GpsCacheController {

    CategoryApi categoryApi;
    QuadTree<List<String>> quadTree;
    private double x, y;
    private double xMinus, xPlus, yMinus, yPlus;

    private static final int EARTH_RADIUS = 6378137;

    @Inject
    public GpsCacheController(CategoryApi categoryApi, QuadTree<List<String>> quadTree){
        this.categoryApi=categoryApi;
        this.quadTree=quadTree;
    }
    /**
     * @param decLongitude
     * @param decLatitude
     * @return
     */
    public Single<List<String>> findCategories(GPSCoordinates coords) {
        if(!coords.hasCoords)
            return Single.just(Collections.EMPTY_LIST);

        x = coords.decLongitude;
        y = coords.decLatitude;
        Timber.d("X (longitude) value: %f, Y (latitude) value: %f", x, y);
        Point<List<String>>[] pointsFound;
        //Convert decLatitude and decLongitude to a coordinate offset range
        convertCoordRange();
        return Observable.fromArray(quadTree.searchWithin(xMinus, yMinus, xPlus, yPlus))
                .flatMap(point -> Observable.fromIterable(point.getValue()))
                .toList()
                .flatMap(list -> list.size() > 0 ?
                        Single.just(list) : //if the list has items, don't bother searching again
                        categoryApi //otherwise, fetch from online
                                .request(coords.decimalCoords)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .doOnSuccess(newList -> quadTree.set(x, y, newList)))
                .observeOn(Schedulers.io());
    }

    //Based on algorithm at http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
    private void convertCoordRange() {
        //Position, decimal degrees
        double lat = y;
        double lon = x;

        //offsets in meters
        double offset = 100;

        //Coordinate offsets in radians
        double dLat = offset / EARTH_RADIUS;
        double dLon = offset / (EARTH_RADIUS * Math.cos(Math.PI * lat / 180));

        //OffsetPosition, decimal degrees
        yPlus = lat + dLat * 180 / Math.PI;
        yMinus = lat - dLat * 180 / Math.PI;
        xPlus = lon + dLon * 180 / Math.PI;
        xMinus = lon - dLon * 180 / Math.PI;
        Timber.d("Search within: xMinus=%s, yMinus=%s, xPlus=%s, yPlus=%s",
                xMinus, yMinus, xPlus, yPlus);
    }
}
