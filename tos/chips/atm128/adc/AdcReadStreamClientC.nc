/* $Id: AdcReadStreamClientC.nc,v 1.1.2.3 2006-02-16 19:02:14 idgay Exp $
 * Copyright (c) 2005 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */
/**
 * Provide, as per TEP101, arbitrated access via a ReadStream interface to
 * the Atmega128 ADC.  Users of this component must link it to an
 * implementation of Atm128AdcConfig which provides the ADC parameters
 * (channel, etc).
 * 
 * @author David Gay
 */

#include "Adc.h"

generic configuration AdcReadStreamClientC() {
  provides interface ReadStream<uint16_t>;
  uses {
    interface Atm128AdcConfig;
    interface ResourceConfigure;
  }
}
implementation {
  components WireAdcStreamP, Atm128AdcC;

  enum {
    ID = unique(UQ_ADC_READSTREAM),
    HAL_ID = unique(UQ_ATM128ADC_RESOURCE)
  };

  ReadStream = WireAdcStreamP.ReadStream[ID];
  Atm128AdcConfig = WireAdcStreamP.Atm128AdcConfig[ID];
  WireAdcStreamP.Resource[ID] -> Atm128AdcC.Resource[HAL_ID];
  ResourceConfigure = Atm128AdcC.ResourceConfigure[HAL_ID];
}
