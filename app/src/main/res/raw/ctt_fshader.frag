#extension GL_OES_EGL_image_external : require
precision mediump float; // Set the default precision to medium. We don't need as high of a precision in the fragment shader.

uniform samplerExternalOES u_Texture;    // The input texture.

varying vec4 v_Color;// This is the color from the vertex shader interpolated across the triangle per fragment.
varying vec2 v_UV; // Interpolated texture coordinate per fragment.

void main()// The entry point for our fragment shader.
{
    gl_FragColor = v_Color * texture2D(u_Texture, v_UV); // Pass the color directly through the pipeline.
    // (Multiply the color by -the diffuse illumination level and- texture value to get final output color.)
}