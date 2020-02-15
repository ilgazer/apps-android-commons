package fr.free.nrw.commons.cache

import com.github.varunpant.quadtree.Point
import com.github.varunpant.quadtree.QuadTree
import com.nhaarman.mockitokotlin2.times
import fr.free.nrw.commons.caching.GpsCacheController
import fr.free.nrw.commons.mwapi.CategoryApi
import fr.free.nrw.commons.upload.metadata.GPSCoordinates
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class CacheControllerTest {
    /**
     * initial setup, test environment
     */
    private lateinit var gpsCacheController: GpsCacheController

    @Mock
    private lateinit var quadTree: QuadTree<List<String>>

    @Mock
    private lateinit var categoryApi: CategoryApi

    private lateinit var gpsCoordinates: GPSCoordinates

    private lateinit var points: Array<Point<List<String>>>

    private lateinit var value: List<String>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        gpsCoordinates = GPSCoordinates.from(
                "0/1,0/1,00/1",
                "N",
                "0/1,0/1,00/1",
                "W");

        value = listOf("1")
        val point = Point<List<String>>(1.0, 1.0, value)
        points = arrayOf(point)

        Mockito.`when`(
                categoryApi.request(
                        ArgumentMatchers.anyString()))
                .thenReturn(Single.just(listOf("test", "test2")))
    }

    @Test
    @Throws(Exception::class)
    fun testFindCachedCategory() {

        Mockito.`when`(
                quadTree.searchWithin(
                        ArgumentMatchers.anyDouble(),
                        ArgumentMatchers.anyDouble(),
                        ArgumentMatchers.anyDouble(),
                        ArgumentMatchers.anyDouble()))
                .thenReturn(points)

        gpsCacheController = GpsCacheController(categoryApi, quadTree)
        val findCategory = gpsCacheController.findCategories(gpsCoordinates)

        assert(findCategory.blockingGet().size == 1)

        verify(quadTree).searchWithin(
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble())

        verify(categoryApi, times(0)).request(
                ArgumentMatchers.anyString())
    }

    @Test
    @Throws(Exception::class)
    fun testFindNewCategory() {

        Mockito.`when`(
                quadTree.searchWithin(
                        ArgumentMatchers.anyDouble(),
                        ArgumentMatchers.anyDouble(),
                        ArgumentMatchers.anyDouble(),
                        ArgumentMatchers.anyDouble()))
                .thenReturn(arrayOf())

        gpsCacheController = GpsCacheController(categoryApi, quadTree)
        val findCategory = gpsCacheController.findCategories(gpsCoordinates)

        assert(findCategory.blockingGet().size == 2)

        verify(quadTree).searchWithin(
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble())

        verify(categoryApi).request(
                ArgumentMatchers.anyString())

        verify(quadTree).set(
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyList())


    }
}