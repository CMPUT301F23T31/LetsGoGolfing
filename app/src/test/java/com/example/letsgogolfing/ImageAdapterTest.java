package com.example.letsgogolfing;

import android.content.Context;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ImageAdapterTest {

    private ImageAdapter imageAdapter;
    private List<Uri> mockUris;

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
        mockUris = Arrays.asList(mock(Uri.class), mock(Uri.class));
        Context context = ApplicationProvider.getApplicationContext();
        imageAdapter = new ImageAdapter(context, mockUris);

    }

    @Test
    public void testGetItemCount() {
        assertEquals(mockUris.size(), imageAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        ViewGroup mockParent = mock(ViewGroup.class);
        ImageAdapter.ImageViewHolder viewHolder = imageAdapter.onCreateViewHolder(mockParent, 0);
        assertEquals(ImageView.class, viewHolder.imageView.getClass());
    }

    @Test
    public void testOnBindViewHolder() {
        ImageAdapter.ImageViewHolder mockHolder = mock(ImageAdapter.ImageViewHolder.class);
        imageAdapter.onBindViewHolder(mockHolder, 0);
        verify(mockHolder).bind(mockUris.get(0));
    }
}