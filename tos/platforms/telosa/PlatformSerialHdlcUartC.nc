/* Copyright (c) 2011 University of California, Berkeley
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the People Power Corporation nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * PEOPLE POWER CO. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE
 *
 */

/** 
 * @author Stephen Dawson-Haggerty <stevedh@eecs.berkeley.edu>
 */
configuration PlatformSerialHdlcUartC {
  provides {
    interface StdControl;
    interface HdlcUart;
  }
} implementation {
#warning Enabling DMA on UART1RX

  components NoLedsC as Leds;
  components PlatformSerialHdlcUartP as HdlcUartC;
  components TelosSerialP;

  StdControl = HdlcUartC;
  HdlcUart = HdlcUartC;

  components new Msp430Usart1C() as UsartC;
  HdlcUartC.UsartResource -> UsartC; 
  UsartC.ResourceConfigure -> HdlcUartC.ResourceConfigure;
  HdlcUartC.Usart -> UsartC;
  HdlcUartC.UsartInterrupts -> UsartC;

  HdlcUartC.Msp430UartConfigure -> TelosSerialP;

  components Msp430DmaC as DmaC;
  HdlcUartC.DmaChannel -> DmaC.Channel2;

  components new Alarm32khz16C();
  HdlcUartC.RxAbort -> Alarm32khz16C;

  HdlcUartC.Leds -> Leds;
}
