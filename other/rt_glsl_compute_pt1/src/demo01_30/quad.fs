#version 130

/* This comes interpolated from the vertex shader */
in vec2 texcoord;

/* The texture we are going to sample */
uniform sampler2D tex;

out vec4 color;

void main(void) {
  /* Well, simply sample the texture */
  color = texture(tex, texcoord);
}
