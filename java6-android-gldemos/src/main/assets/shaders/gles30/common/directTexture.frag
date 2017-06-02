#version 300 es
/**
 * Copyright 2015 Michael Leahy / TyphonRT, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Direct texture fragment shader

precision mediump float;

uniform sampler2D uSourceTex;
uniform sampler2D uSourceTex2;
// uniform mat4 u_MVPMatrix;

in vec2 vTexCoord;
out vec4 color;

void main(void)                    // The entry point for our fragment shader.
{
    vec4 disp = texture(uSourceTex, vTexCoord);
////   if (disp.a < 0.1) {
////      color = vec4(0.0);
////      return;
////   }
    color = texture(uSourceTex2, disp.rg);
//    color = texture(uSourceTex, vTexCoord);
}