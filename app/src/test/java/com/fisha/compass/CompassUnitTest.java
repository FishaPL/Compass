package com.fisha.compass;

import android.location.Location;

import com.fisha.compass.viewModel.CompassViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CompassUnitTest
{
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    Location location;
    Location target;

    @Mock
    LifecycleOwner lifecycleOwner;
    Lifecycle lifecycle;

    private CompassViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        lifecycle = new LifecycleRegistry(lifecycleOwner);
        //viewModel = new CompassViewModel(context);
        location = mock(Location.class);
        target = mock(Location.class);
    }

    @Test
    public void updateNewDistance_isCorrect()
    {
        assertEquals(4, 2 + 2);
        assertTrue(true);
    }

}
