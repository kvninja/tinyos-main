/*									tab:4
 *
 *
 * "Copyright (c) 2000-2002 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 */
/*
 *
 * Authors:		Joe Polastre
 * Date last modified:  $Revision: 1.1.2.1 $
 *
 * The RadioPacket interface allows link-layer dependent packet layout
 * but hardware independent access to the fields of a TOSMsg.
 * By passing a TOSMsg to the RadioPacket interface, you can extract 
 * (and reuse) common TOSMsg fields.  For applications that are built
 * for only a single platform, they may use the TOSMsg.header,
 * TOSMsg.footer, and TOSMsg.metadata definitions instead with the specific
 * header, footer, and metadata definitions for that radio chip.
 */

/**
 * Radio packet interface for reading packet data in a platform
 * independent manner
 */
interface RadioPacket
{
  /**
   * Gets the length of the current TOSMsg data payload
   */
  command uint8_t getLength(TOSMsg* msg);
  /**
   * Sets the length of the current TOSMsg data payload
   */
  command result_t setLength(TOSMsg* msg, uint8_t length);

  /**
   * Gets a pointer to the data payload of the message
   */
  command uint8_t* getData(TOSMsg* msg);

  /**
   * Gets the destination address of the specified TOSMsg
   */
  command uint16_t getAddress(TOSMsg* msg);
  /**
   * Sets the destination address of the specified TOSMsg
   */
  command result_t setAddress(TOSMsg* msg, uint16_t addr);

  /**
   * Gets the destination logical group of the specified TOSMsg
   */
  command uint16_t getGroup(TOSMsg* msg);
  /**
   * Sets the destination logical group of the specified TOSMsg
   */
  command result_t setGroup(TOSMsg* msg, uint16_t group);

  /**
   * Gets a 16-bit 32768Hz time value corresponding to the transmitted
   * SFD on a transmitted packet, or when the SFD was received for a 
   * incoming packet.
   */
  command uint16_t getTime(TOSMsg* msg);

  /**
   * Indicates whether an acknowledgment has been received.
   * If acknowledgments are enabled in the CSMAControl interface,
   * isAck returns TRUE for a transmitted message where a corresponding
   * acknowledgment has been received, otherwise it returns FALSE if no
   * ack for this TOSMsg has been received.
   */
  command bool isAck(TOSMsg* msg);
}
