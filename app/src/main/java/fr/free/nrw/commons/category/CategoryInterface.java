package fr.free.nrw.commons.category;

import org.wikipedia.dataclient.mwapi.MwQueryResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for interacting with Commons category related APIs
 */
public interface CategoryInterface {

    /**
     * Searches for categories with the specified name.
     *
     * @param filter    The string to be searched
     * @param itemLimit How many results are returned
     * @return
     */
    @GET("w/api.php?action=query&format=json&formatversion=2"
            + "&generator=search&gsrnamespace=14")
    Observable<MwQueryResponse> searchCategories(@Query("gsrsearch") String filter,
                                                 @Query("gsrlimit") int itemLimit, @Query("gsroffset") int offset);

    /**
     * Searches for categories starting with the specified prefix.
     *
     * @param prefix    The string to be searched
     * @param itemLimit How many results are returned
     * @return
     */
    @GET("w/api.php?action=query&format=json&formatversion=2"
            + "&generator=allcategories")
    Observable<MwQueryResponse> searchCategoriesForPrefix(@Query("gacprefix") String prefix,
                                                          @Query("gaclimit") int itemLimit, @Query("gacoffset") int offset);

    @GET("w/api.php?action=query&format=json&formatversion=2"
            + "&generator=categorymembers&gcmtype=subcat"
            + "&prop=info&gcmlimit=500")
    Observable<MwQueryResponse> getSubCategoryList(@Query("gcmtitle") String categoryName);

    @GET("w/api.php?action=query&format=json&formatversion=2"
            + "&generator=categories&prop=info&gcllimit=500")
    Observable<MwQueryResponse> getParentCategoryList(@Query("titles") String categoryName);

    /**
     * @param searchCoords
     * @param radius
     * @param centerCoords
     * @param limit
     * @return
     */
    @GET("w/api.php?action=query&format=json&formatversion=2" +
            "&generator=geosearch&ggsnamespace=6" +
            "&prop=categories|coordinates|pageprops&clshow=!hidden" +
            "&coprop=type|name|dim|country|region|globe" +
            "&ggsprop=type|name|dim|country|region|globe&ggsprimary=all")
    Observable<MwQueryResponse> getGpsCategories(@Query("ggscoord") String searchCoords,
                                                 @Query("ggsradius") int radius,
                                                 @Query("codistancefrompoint") String centerCoords,
                                                 @Query("ggslimit") int limit);
}
